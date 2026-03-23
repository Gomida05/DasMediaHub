package com.das.mediaHub.data.downloader

import android.content.Context
import com.das.mediaHub.data.local.DownloadStateStore
import com.das.mediaHub.data.model.download.DownloadState
import com.das.mediaHub.data.model.download.DownloadState.Companion.toDownloadState
import com.das.mediaHub.data.model.download.DownloadStatus
import com.das.mediaHub.data.model.download.DownloadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DownloadQueueManager(
    context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val appContext = context.applicationContext

    private val store by lazy {
        DownloadStateStore(appContext)
    }

    private val downloader by lazy {
        ResumableDownloader(
            OkHttpClient.Builder().build()
        )
    }

    private val pauseFlags = ConcurrentHashMap<String, Boolean>()
    private val cancelFlags = ConcurrentHashMap<String, Boolean>()

    private val _states = MutableStateFlow<List<DownloadState>>(emptyList())
    val states = _states.asStateFlow()

    @Volatile
    private var workerRunning = false

    @Volatile
    private var restored = false

    suspend fun restore() {
        if (restored) return
        _states.value = store.loadAll()
        restored = true
        startWorkerIfNeeded()
    }

    fun enqueue(task: DownloadTask) {
        val newState = task.toDownloadState()

        val updated = _states.value
            .filterNot { it.id == task.id } + newState

        persistAndPublish(updated)
        startWorkerIfNeeded()
    }

    fun enqueuePlaylist(tasks: List<DownloadTask>) {
        val existing = _states.value.toMutableList()

        tasks.forEach { task ->
            existing.removeAll { it.id == task.id }
            existing.add(task.toDownloadState())
        }

        persistAndPublish(existing)
        startWorkerIfNeeded()
    }

    fun pause(id: String) {
        pauseFlags[id] = true
    }

    fun resume(id: String) {
        pauseFlags[id] = false
        cancelFlags.remove(id)

        val updated = _states.value.map { state ->
            if (state.id == id && (
                        state.status == DownloadStatus.PAUSED ||
                                state.status == DownloadStatus.FAILED ||
                                state.status == DownloadStatus.CANCELED
                        )
            ) {
                if (state.status == DownloadStatus.CANCELED) {
                    state.copy(
                        status = DownloadStatus.QUEUED,
                        progress = 0,
                        downloadedBytes = 0L,
                        totalBytes = -1L,
                        errorMessage = null
                    )
                } else {
                    state.copy(
                        status = DownloadStatus.QUEUED,
                        errorMessage = null
                    )
                }
            } else {
                state
            }
        }

        persistAndPublish(updated)
        startWorkerIfNeeded()
    }

    fun cancel(id: String) {
        cancelFlags[id] = true

        val current = _states.value.firstOrNull { it.id == id }
        if (current != null && current.status != DownloadStatus.DOWNLOADING) {
            File(current.destinationPath).delete()
            val updated = _states.value.map {
                if (it.id == id) {
                    it.copy(
                        status = DownloadStatus.CANCELED,
                        progress = 0,
                        downloadedBytes = 0L,
                        totalBytes = -1L,
                        errorMessage = null
                    )
                } else {
                    it
                }
            }
            persistAndPublish(updated)
        }
    }

    fun removeFinished(id: String) {
        val updated = _states.value.filterNot { it.id == id }
        persistAndPublish(updated)
    }

    fun getState(id: String): DownloadState? = _states.value.firstOrNull { it.id == id }

    private fun startWorkerIfNeeded() {
        if (workerRunning) return
        workerRunning = true

        scope.launch {
            while (true) {
                val next = _states.value.firstOrNull { it.status == DownloadStatus.QUEUED } ?: break
                runTask(next)
            }
            workerRunning = false
        }
    }

    private suspend fun runTask(state: DownloadState) {
        updateState(state.id) {
            it.copy(status = DownloadStatus.DOWNLOADING, errorMessage = null)
        }

        val task = DownloadTask(
            id = state.id,
            url = state.url,
            title = state.title,
            type = state.type,
            destinationPath = state.destinationPath,
            playlistName = state.playlistName
        )

        val outcome = downloader.download(
            task = task,
            alreadyDownloadedBytes = state.downloadedBytes,
            isPaused = { pauseFlags[state.id] == true },
            isCanceled = { cancelFlags[state.id] == true }
        ) { downloaded, total ->
            val progress = if (total > 0) ((downloaded * 100) / total).toInt() else 0
            updateState(state.id) {
                it.copy(
                    status = DownloadStatus.DOWNLOADING,
                    downloadedBytes = downloaded,
                    totalBytes = total,
                    progress = progress.coerceIn(0, 100)
                )
            }
        }

        when (outcome) {
            is ResumableDownloader.Outcome.Completed -> {
                pauseFlags.remove(state.id)
                cancelFlags.remove(state.id)
                updateState(state.id) {
                    it.copy(
                        status = DownloadStatus.COMPLETED,
                        progress = 100,
                        errorMessage = null
                    )
                }
            }

            is ResumableDownloader.Outcome.Paused -> {
                updateState(state.id) {
                    it.copy(status = DownloadStatus.PAUSED)
                }
            }

            is ResumableDownloader.Outcome.Canceled -> {
                pauseFlags.remove(state.id)
                cancelFlags.remove(state.id)
                updateState(state.id) {
                    it.copy(
                        status = DownloadStatus.CANCELED,
                        progress = 0,
                        downloadedBytes = 0L,
                        totalBytes = -1L,
                        errorMessage = null
                    )
                }
            }

            is ResumableDownloader.Outcome.Failed -> {
                pauseFlags.remove(state.id)
                cancelFlags.remove(state.id)
                updateState(state.id) {
                    it.copy(
                        status = DownloadStatus.FAILED,
                        errorMessage = outcome.message
                    )
                }
            }
        }
    }

    private fun updateState(id: String, transform: (DownloadState) -> DownloadState) {
        val updated = _states.value.map { if (it.id == id) transform(it) else it }
        persistAndPublish(updated)
    }

    private fun persistAndPublish(list: List<DownloadState>) {
        _states.value = list
        scope.launch {
            store.saveAll(list)
        }
    }

    fun shutdown() {
        scope.cancel()
    }

    companion object {
        @Volatile
        private var INSTANCE: DownloadQueueManager? = null

        fun get(context: Context): DownloadQueueManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DownloadQueueManager(context).also { INSTANCE = it }
            }
        }

        fun newTaskId(): String = UUID.randomUUID().toString()
    }
}
package com.das.downloader.data.downloader

import com.das.downloader.data.local.DownloadStateStore
import com.das.downloader.data.model.Outcome
import com.das.downloader.data.model.download.DownloadState
import com.das.downloader.data.model.download.DownloadState.Companion.toDownloadState
import com.das.downloader.data.model.download.DownloadStatus
import com.das.downloader.data.model.download.DownloadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Manager that orchestrates the download queue, task execution, and state persistence.
 * 
 * It manages a background worker that processes [DownloadTask]s sequentially,
 * updates their progress via [MutableStateFlow], and saves the state to [DownloadStateStore].
 * 
 * This class is designed to be decoupled from Android components by receiving its 
 * dependencies (store and scope) via the constructor.
 * 
 * Example usage:
 * ```kotlin
 * val manager = DownloadQueueManager(
 *     store = downloadStateStore,
 *     downloader = downloader,
 *     scope = lifecycleScope // or a custom CoroutineScope
 * )
 * manager.states.collect { states ->
 *     // Update UI with download progress
 * }
 * ```
 */
class DownloadQueueManager(
    private val store: DownloadStateStore,
    private val downloader: Downloader,
    private val scope: CoroutineScope
) {



    private val _states = MutableStateFlow<List<DownloadState>>(emptyList())
    /**
     * Flow of all download states, including queued, downloading, paused, and completed tasks.
     */
    val states = _states.asStateFlow()
    private val pauseFlags = ConcurrentHashMap<String, Boolean>()

    // 1. Replaced cancelFlags with a map to track active Coroutine Jobs
    private val activeJobs = ConcurrentHashMap<String, Job>()



    @Volatile
    private var workerRunning = false

    @Volatile
    private var restored = false

    /**
     * Restores the download queue from persistent storage.
     * Should be called during application startup.
     */
    suspend fun restore() {
        if (restored) return
        _states.value = store.loadAll()
        restored = true
        startWorkerIfNeeded()
    }

    /**
     * Adds a single task to the queue.
     * @param task The task to be enqueued.
     */
    fun enqueue(task: DownloadTask) {
        val newState = task.toDownloadState()
        val updated = _states.value.filterNot { it.id == task.id } + newState
        persistAndPublish(updated)
        startWorkerIfNeeded()
    }

    /**
     * Adds a list of tasks as part of a playlist.
     * @param tasks The list of tasks to be enqueued.
     */
    fun enqueuePlaylist(tasks: List<DownloadTask>) {
        val existing = _states.value.toMutableList()
        tasks.forEach { task ->
            existing.removeAll { it.id == task.id }
            existing.add(task.toDownloadState())
        }
        persistAndPublish(existing)
        startWorkerIfNeeded()
    }

    /**
     * Requests a task to be paused.
     * @param id The unique task ID.
     */
    fun pause(id: String) {
        pauseFlags[id] = true
    }

    /**
     * Resumes a paused, failed, or canceled task.
     * @param id The unique task ID.
     */
    fun resume(id: String) {
        pauseFlags[id] = false

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

    /**
     * Requests a task to be canceled.
     * @param id The unique task ID.
     */
    fun cancel(id: String) {
        // 2. Cancel the coroutine job if it is currently running
        activeJobs[id]?.cancel()

        val current = _states.value.firstOrNull { it.id == id }
        if (current != null) {
            // 3. If it's NOT downloading, we update the state here.
            // If it IS downloading, the cancellation will be caught in runTask() and updated there.
            if (current.status != DownloadStatus.DOWNLOADING) {
                File(current.destinationPath).delete()
                updateState(id) {
                    it.copy(
                        status = DownloadStatus.CANCELED,
                        progress = 0,
                        downloadedBytes = 0L,
                        totalBytes = -1L,
                        errorMessage = null
                    )
                }
            }
        }
    }

    /**
     * Removes a finished task from the list of states.
     * @param id The unique task ID.
     */
    fun removeFinished(id: String) {
        val updated = _states.value.filterNot { it.id == id }
        persistAndPublish(updated)
    }

    /**
     * Retrieves the current state of a specific download task.
     * @param id The unique task ID.
     * @return The [DownloadState] or null if not found.
     */
    fun getState(id: String): DownloadState? = _states.value.firstOrNull { it.id == id }

    /**
     * Starts the sequential processing worker if it's not already running.
     */
    private fun startWorkerIfNeeded() {
        if (workerRunning) return
        workerRunning = true

        scope.launch {
            while (true) {
                val next = _states.value.firstOrNull { it.status == DownloadStatus.QUEUED } ?: break

                val taskJob = launch {
                    runTask(next)
                }
                activeJobs[next.id] = taskJob

                // Suspend the loop until this specific task finishes or is canceled
                taskJob.join()
                activeJobs.remove(next.id)
            }
            workerRunning = false
        }
    }

    /**
     * Executes a single download task using the [ResumableDownloader].
     */
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

        // 5. Removed the isCanceled lambda
        val outcome = downloader.download(
            task = task,
            alreadyDownloadedBytes = state.downloadedBytes,
            isPaused = { pauseFlags[state.id] == true }
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
            is Outcome.Completed -> {
                pauseFlags.remove(state.id)
                updateState(state.id) {
                    it.copy(
                        status = DownloadStatus.COMPLETED,
                        progress = 100,
                        errorMessage = null
                    )
                }
            }

            is Outcome.Paused -> {
                updateState(state.id) {
                    it.copy(status = DownloadStatus.PAUSED)
                }
            }

            is Outcome.Canceled -> {
                pauseFlags.remove(state.id)
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

            is Outcome.Failed -> {
                pauseFlags.remove(state.id)
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

    /**
     * Cleans up resources and cancels active coroutines.
     */
    fun shutdown() {
        scope.cancel()
    }

    companion object {
        /**
         * Generates a unique task ID.
         */
        fun newTaskId(): String = UUID.randomUUID().toString()
    }
}

package com.das.downloader

import com.das.downloader.data.downloader.Downloader
import com.das.downloader.data.local.DownloadPreferences
import com.das.downloader.data.local.DownloadStateStore
import com.das.downloader.data.model.Outcome
import com.das.downloader.data.model.download.DownloadState
import com.das.downloader.data.model.download.DownloadState.Companion.toDownloadState
import com.das.downloader.data.model.download.DownloadStatus
import com.das.downloader.data.model.download.DownloadTask
import com.das.downloader.data.network.NetworkStatusProvider
import android.content.Context
import android.media.MediaScannerConnection
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

/**
 * Manager that orchestrates the download queue, task execution, and state persistence.
 *
 * It manages a background worker that processes [com.das.downloader.data.model.download.DownloadTask]s,
 * updates their progress via [kotlinx.coroutines.flow.MutableStateFlow], and saves the state to [com.das.downloader.data.local.DownloadStateStore].
 *
 * This version supports concurrent downloads and respects network rules defined in [DownloadPreferences].
 *
 * @param context Android context for accessing preferences.
 * @param store Storage for persisting download states.
 * @param downloader Implementation of the downloader.
 * @param networkProvider Provider for checking network status.
 * @param scope CoroutineScope for running background tasks.
 */
class DownloadQueueManager(
    private val context: Context,
    private val store: DownloadStateStore,
    private val downloader: Downloader,
    private val networkProvider: NetworkStatusProvider,
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
     * Adds a single state directly.
     */
    fun enqueueState(state: DownloadState) {
        val updated = _states.value.filterNot { it.id == state.id } + state
        persistAndPublish(updated)
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
     * Checks if a task is currently requested to be paused.
     */
    fun isPaused(id: String): Boolean = pauseFlags[id] == true

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

        // 3. Cancel WorkManager job if it exists (for background tasks)
        try {
            WorkManager.getInstance(context).cancelAllWorkByTag(id)
        } catch (_: Exception) {
        }

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
     * Updates the progress of an externally managed task.
     */
    fun updateExternalProgress(id: String, status: DownloadStatus, downloaded: Long, total: Long, progress: Int, speed: Long = 0L) {
        updateState(id) {
            it.copy(
                status = status,
                downloadedBytes = downloaded,
                totalBytes = total,
                progress = progress,
                downloadSpeed = speed
            )
        }
    }

    /**
     * Marks an externally managed task as failed.
     */
    fun markAsFailed(id: String, message: String) {
        updateState(id) {
            it.copy(
                status = DownloadStatus.FAILED,
                errorMessage = message
            )
        }
    }

    /**
     * Retrieves the current state of a specific download task.
     * @param id The unique task ID.
     * @return The [DownloadState] or null if not found.
     */
    fun getState(id: String): DownloadState? = _states.value.firstOrNull { it.id == id }

    /**
     * Starts the processing worker if it's not already running.
     * Manages multiple concurrent downloads based on user preferences.
     */
    private fun startWorkerIfNeeded() {
        if (workerRunning) return
        workerRunning = true

        scope.launch {
            while (true) {
                val maxConcurrent = DownloadPreferences.getMaxConcurrentDownloads(context)
                val currentRunning = activeJobs.size

                if (currentRunning >= maxConcurrent) {
                    continue
                }

                val next = _states.value.firstOrNull { it.status == DownloadStatus.QUEUED }
                if (next == null) {
                    if (currentRunning == 0) break else {
                        continue
                    }
                }

                // Check network rules before starting
                if (!canDownloadNow()) {
                    continue
                }

                val taskJob = launch {
                    runTask(next)
                }
                activeJobs[next.id] = taskJob
                
                // Allow some time for state update to reflect in activeJobs.size or just continue the loop
                delay(60.milliseconds)
            }
            workerRunning = false
        }
    }

    /**
     * Checks if a download can proceed based on network connectivity and user settings.
     */
    private fun canDownloadNow(): Boolean {
        if (!networkProvider.isConnected()) return false
        
        val allowOverData = DownloadPreferences.getDownloadOverMobileData(context)
        if (!allowOverData && networkProvider.isMetered()) {
            return false
        }
        
        return true
    }

    /**
     * Executes a single download task using the [com.das.downloader.data.downloader.ResumableDownloader].
     */
    private suspend fun runTask(state: DownloadState) {
        val tempFile = File(context.cacheDir, "temp_queue_${state.id}_${state.title.hashCode()}")
        
        try {
            updateState(state.id) {
                it.copy(status = DownloadStatus.DOWNLOADING, errorMessage = null)
            }

            val task = DownloadTask(
                id = state.id,
                url = state.url,
                title = state.title,
                type = state.type,
                destinationPath = tempFile.absolutePath,
                playlistName = state.playlistName
            )

            var lastBytes = state.downloadedBytes
            var lastTime = System.currentTimeMillis()

            val outcome = downloader.download(
                task = task,
                alreadyDownloadedBytes = if (tempFile.exists()) tempFile.length() else 0L,
                isPaused = { 
                    pauseFlags[state.id] == true || !canDownloadNow()
                }
            ) { downloaded, total ->
                val currentTime = System.currentTimeMillis()
                val timeDiff = currentTime - lastTime
                
                var speed = 0L
                if (timeDiff >= 1000) {
                    speed = ((downloaded - lastBytes) * 1000) / timeDiff
                    lastBytes = downloaded
                    lastTime = currentTime
                }

                val progress = if (total > 0) ((downloaded * 100) / total).toInt() else 0
                updateState(state.id) {
                    it.copy(
                        status = DownloadStatus.DOWNLOADING,
                        downloadedBytes = downloaded,
                        totalBytes = total,
                        progress = progress.coerceIn(0, 100),
                        downloadSpeed = if (speed > 0) speed else it.downloadSpeed
                    )
                }
            }

            when (outcome) {
                is Outcome.Completed -> {
                    pauseFlags.remove(state.id)
                    
                    // Move file from cache to final destination
                    val finalFile = File(state.destinationPath)
                    finalFile.parentFile?.mkdirs()
                    
                    val moveSuccessful = if (tempFile.renameTo(finalFile)) {
                        true
                    } else {
                        try {
                            tempFile.copyTo(finalFile, overwrite = true)
                            tempFile.delete()
                            true
                        } catch (_: Exception) {
                            false
                        }
                    }

                    if (moveSuccessful) {
                        // Scan the file so it appears in the gallery/music player
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(state.destinationPath),
                            null,
                            null
                        )

                        updateState(state.id) {
                            it.copy(
                                status = DownloadStatus.COMPLETED,
                                progress = 100,
                                errorMessage = null
                            )
                        }
                    } else {
                        updateState(state.id) {
                            it.copy(
                                status = DownloadStatus.FAILED,
                                errorMessage = "Failed to move file to destination"
                            )
                        }
                    }
                }

                is Outcome.Paused -> {
                    updateState(state.id) {
                        it.copy(status = DownloadStatus.PAUSED)
                    }
                }

                is Outcome.Canceled -> {
                    pauseFlags.remove(state.id)
                    tempFile.delete()
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
                    // We don't delete the temp file on failure so it can be resumed
                    updateState(state.id) {
                        it.copy(
                            status = DownloadStatus.FAILED,
                            errorMessage = outcome.message
                        )
                    }
                }
            }
        } catch (e: Exception) {
            tempFile.delete()
            throw e
        } finally {
            activeJobs.remove(state.id)
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
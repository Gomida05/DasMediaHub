package com.das.mediaHub.services.download

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaScannerConnection
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.das.downloader.DownloadQueueManager
import com.das.downloader.data.downloader.DownloadRequest
import com.das.downloader.data.downloader.Downloader
import com.das.downloader.data.model.Outcome
import com.das.downloader.data.model.download.DownloadState.Companion.toDownloadState
import com.das.downloader.data.model.download.DownloadStatus
import com.das.downloader.data.repository.MediaDownloadRepository
import com.das.mediaHub.NotificationChannels.NotificationChannelNames.DOWNLOAD_CHANNEL
import com.das.mediaHub.R
import com.das.mediaHub.data.error.ErrorMapper
import com.das.python.PythonMain.decodeStringToJson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * A [CoroutineWorker] that handles background media downloads (YouTube, Social Media).
 *
 * This worker resolves the [DownloadRequest] using [MediaDownloadRepository],
 * manages the foreground state with notifications, and executes the download
 * using [Downloader].
 *
 * Example usage via [DownloadDispatcher]:
 * ```kotlin
 * val request = DownloadRequest.YoutubeVideo(videoId = "...", title = "My Video")
 * DownloadDispatcher.enqueue(context, request)
 * ```
 */
@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val repository: MediaDownloadRepository,
    private val downloader: Downloader,
    private val queueManager: DownloadQueueManager
) : CoroutineWorker(appContext, params) {

    private val notificationManager by lazy {
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var currentTitle: String = "Media Download"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val requestJson = inputData.getString(KEY_DOWNLOAD_REQUEST)
            if (requestJson == null) {
                Log.e(TAG, "No download request provided in input data.")
                return@withContext Result.failure()
            }

            val request = try {
                requestJson.decodeStringToJson<DownloadRequest>()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode DownloadRequest: ${e.message}")
                return@withContext Result.failure()
            }

            // 2. Start foreground service
            setForegroundAsync(createForegroundInfo())

            currentTitle = request.title

            // 1. Resolve request to task (stream URLs + file path)
            val task = repository.resolveTask(request)
            
            // Register in the queue manager so it shows in the UI
            val taskId = inputData.getString("KEY_TASK_ID") ?: params.id.toString()
            
            // Use cache directory for temp file
            val tempFile = File(appContext.cacheDir, "temp_${taskId}_${task.title.hashCode()}")
            val initialTask = task.copy(id = taskId, destinationPath = tempFile.absolutePath)
            
            val initialState = initialTask.toDownloadState().copy(
                status = DownloadStatus.DOWNLOADING,
                request = request,
                destinationPath = task.destinationPath // Show the real final path in UI
            )
            queueManager.enqueueState(initialState)
            queueManager.updateExternalProgress(taskId, DownloadStatus.DOWNLOADING, 0L, -1L, 0)

            // 3. Execute download
            var lastBytes = 0L
            var lastTime = System.currentTimeMillis()
            
            val outcome = downloader.download(
                task = initialTask,
                alreadyDownloadedBytes = 0L, // New download for now, can be improved to support resume
                isPaused = { isStopped || queueManager.isPaused(taskId) },
                onProgress = { downloaded, total ->
                    val currentTime = System.currentTimeMillis()
                    val timeDiff = currentTime - lastTime
                    
                    var speed = 0L
                    if (timeDiff >= 1000) {
                        speed = ((downloaded - lastBytes) * 1000) / timeDiff
                        lastBytes = downloaded
                        lastTime = currentTime
                    }

                    if (total > 0) {
                        val progress = ((downloaded * 100) / total).toInt()
                        updateProgressSync(progress, speed)
                        
                        // Update UI manager
                        queueManager.updateExternalProgress(
                            id = taskId,
                            status = DownloadStatus.DOWNLOADING,
                            downloaded = downloaded,
                            total = total,
                            progress = progress,
                            speed = if (speed > 0) speed else (queueManager.getState(taskId)?.downloadSpeed ?: 0L)
                        )
                    }
                }
            )

            when (outcome) {
                is Outcome.Completed -> {
                    // Move file from cache to final destination
                    val finalFile = File(task.destinationPath)
                    finalFile.parentFile?.mkdirs()
                    if (tempFile.renameTo(finalFile)) {
                         // Success
                    } else {
                        // Fallback: Copy if rename fails (across partitions)
                        tempFile.copyTo(finalFile, overwrite = true)
                        tempFile.delete()
                    }

                    // Update manager - we don't know the exact final bytes here easily, 
                    // but we can assume success and 100%
                    val finalState = queueManager.getState(taskId)
                    queueManager.updateExternalProgress(
                        id = taskId,
                        status = DownloadStatus.COMPLETED,
                        downloaded = finalState?.totalBytes ?: 0L,
                        total = finalState?.totalBytes ?: 0L,
                        progress = 100
                    )
                    
                    // Scan the file so it appears in the gallery/music player
                    MediaScannerConnection.scanFile(
                        appContext,
                        arrayOf(task.destinationPath),
                        null,
                        null
                    )
                    showFinishedNotification(task.destinationPath, task.title)
                    Result.success()
                }

                is Outcome.Failed -> {
                    tempFile.delete()
                    val errorMsg = ErrorMapper.mapMessage(outcome.message)
                    queueManager.markAsFailed(taskId, errorMsg)
                    showErrorNotification(errorMsg, taskId, request)
                    Result.failure()
                }

                is Outcome.Paused -> {
                    queueManager.updateExternalProgress(taskId, DownloadStatus.PAUSED, 0, 0, 0)
                    Result.retry()
                }
                is Outcome.Canceled -> {
                    tempFile.delete()
                    queueManager.updateExternalProgress(taskId, DownloadStatus.CANCELED, 0, 0, 0)
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during download: ${e.message}", e)
            val errorMsg = ErrorMapper.map(e)
            val taskId = inputData.getString("KEY_TASK_ID") ?: params.id.toString()
            val requestJson = inputData.getString(KEY_DOWNLOAD_REQUEST)
            val request = requestJson?.decodeStringToJson<DownloadRequest>()
            
            queueManager.markAsFailed(taskId, errorMsg)
            if (request != null) {
                showErrorNotification(errorMsg, taskId, request)
            } else {
                // Fallback for missing request info
                val notification = NotificationCompat.Builder(appContext, DOWNLOAD_CHANNEL)
                    .setContentTitle("Download Failed")
                    .setContentText(errorMsg)
                    .setSmallIcon(R.drawable.download)
                    .setAutoCancel(true)
                    .build()
                notificationManager.notify(currentTitle.hashCode(), notification)
            }
            Result.failure()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = buildProgressNotification(0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun buildProgressNotification(progress: Int, speed: Long = 0L): Notification {
        val speedStr = if (speed > 0) " (${android.text.format.Formatter.formatFileSize(appContext, speed)}/s)" else ""
        
        val taskId = inputData.getString("KEY_TASK_ID") ?: params.id.toString()
        val isPaused = queueManager.isPaused(taskId)

        val cancelIntent = Intent(appContext, DownloadReceiver::class.java).apply {
            action = ACTION_CANCEL
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            appContext,
            taskId.hashCode() + 1,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeIntent = Intent(appContext, DownloadReceiver::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val pauseResumePendingIntent = PendingIntent.getBroadcast(
            appContext,
            taskId.hashCode() + 2,
            pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(appContext, DOWNLOAD_CHANNEL)
            .setContentTitle(currentTitle)
            .setContentText(if (isPaused) "Paused at $progress%" else if (progress > 0) "Downloading... $progress%$speedStr" else "Preparing...")
            .setSmallIcon(R.drawable.download)
            .setOngoing(!isPaused)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress, progress == 0 && !isPaused)
            .addAction(R.drawable.close, "Cancel", cancelPendingIntent)

        if (isPaused) {
            builder.addAction(androidx.media3.ui.compose.material3.R.drawable.media3_icon_play, "Resume", pauseResumePendingIntent)
        } else {
            builder.addAction(androidx.media3.ui.compose.material3.R.drawable.media3_icon_pause, "Pause", pauseResumePendingIntent)
        }

        return builder.build()
    }

    private var lastUpdate = 0L
    private fun updateProgressSync(progress: Int, speed: Long = 0L) {
        val now = System.currentTimeMillis()
        // Force update if paused state changed or enough time passed
        if (now - lastUpdate >= 500) {
            lastUpdate = now
            notificationManager.notify(NOTIFICATION_ID, buildProgressNotification(progress, speed))
        }
    }

    private fun showFinishedNotification(filePath: String, title: String) {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, appContext.contentResolver.getType(uri) ?: "application/*")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, DOWNLOAD_CHANNEL)
            .setContentTitle("Download Finished")
            .setContentText(title)
            .setSmallIcon(R.drawable.download)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(title.hashCode(), notification)
    }

    private fun showErrorNotification(message: String, taskId: String, request: DownloadRequest) {
        val retryIntent = Intent(appContext, DownloadReceiver::class.java).apply {
            action = ACTION_RETRY
            putExtra(EXTRA_DOWNLOAD_REQUEST, Json.encodeToString(request))
        }
        val retryPendingIntent = PendingIntent.getBroadcast(
            appContext,
            taskId.hashCode(),
            retryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, DOWNLOAD_CHANNEL)
            .setContentTitle("Download Failed")
            .setContentText(message)
            .setSmallIcon(R.drawable.download)
            .setAutoCancel(true)
            .addAction(R.drawable.refresh, "Retry", retryPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(currentTitle.hashCode(), notification)
    }

    companion object {
        const val TAG = "DownloadWorker"
        const val KEY_DOWNLOAD_REQUEST = "key_download_request"
        private const val NOTIFICATION_ID = 110033
        
        const val ACTION_RETRY = "com.das.mediaHub.ACTION_RETRY"
        const val ACTION_CANCEL = "com.das.mediaHub.ACTION_CANCEL"
        const val ACTION_PAUSE = "com.das.mediaHub.ACTION_PAUSE"
        const val ACTION_RESUME = "com.das.mediaHub.ACTION_RESUME"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_DOWNLOAD_REQUEST = "extra_download_request"
    }
}
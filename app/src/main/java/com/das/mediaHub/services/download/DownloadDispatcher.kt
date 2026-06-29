package com.das.mediaHub.services.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.das.downloader.DownloadQueueManager
import com.das.downloader.data.downloader.DownloadRequest
import kotlinx.serialization.json.Json

/**
 * Utility for enqueuing media download tasks using [WorkManager] and [DownloadWorker].
 *
 * This centralizes the configuration of constraints and input data for all
 * media download operations (YouTube, Social Media).
 *
 * Example usage from a Composable or ViewModel:
 * ```kotlin
 * val request = DownloadRequest.YoutubeVideo(videoId = "dQw4w9WgXcQ", title = "Never Gonna Give You Up")
 * DownloadDispatcher.enqueue(context, request)
 * ```
 */
object DownloadDispatcher {

    /**
     * Enqueues a [DownloadRequest] to be processed by [DownloadWorker].
     *
     * @param context The application context.
     * @param request The specific download request details.
     */
    fun enqueue(context: Context, request: DownloadRequest) {
        val workManager = WorkManager.getInstance(context)
        val taskId = DownloadQueueManager.newTaskId()

        val inputData = Data.Builder()
            .putString(DownloadWorker.KEY_DOWNLOAD_REQUEST, Json.encodeToString(request))
            .putString("KEY_TASK_ID", taskId)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadWork = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(DownloadWorker.TAG)
            .addTag(taskId)
            .addTag(request.title) // Useful for tracking specific downloads
            .build()

        workManager.enqueue(downloadWork)
    }
}

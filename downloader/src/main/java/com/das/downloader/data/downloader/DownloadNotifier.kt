package com.das.downloader.data.downloader

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.das.downloader.R
import com.das.downloader.data.model.download.DownloadState
import com.das.downloader.data.model.download.DownloadStatus

/**
 * Utility for creating and updating system notifications related to downloads.
 * 
 * It manages the [DOWNLOADER_NOTIFICATION_CHANNEL] and updates notification
 * content based on the [DownloadState] of active tasks.
 * 
 * Example usage:
 * ```kotlin
 * val notifier = DownloadNotifier(context)
 * notifier.notifyState(activeDownloadState)
 * ```
 */
class DownloadNotifier(
    private val context: Context
) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    /**
     * Creates a static notification used for Foreground Services.
     * 
     * @return A low-priority ongoing notification.
     */
    fun foregroundNotification(): Notification {
        return NotificationCompat.Builder(context, DOWNLOADER_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.download)
            .setContentTitle("MediaHub downloads")
            .setContentText("Managing downloads...")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Updates or creates a notification for a specific download state.
     * 
     * The notification content and progress bar are updated according
     * to the [DownloadStatus] of the task.
     * 
     * @param state The current state of the download task.
     */
    fun notifyState(state: DownloadState) {
        val builder = NotificationCompat.Builder(context, DOWNLOADER_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.download)
            .setContentTitle(state.title)
            .setOnlyAlertOnce(true)

        when (state.status) {
            DownloadStatus.QUEUED -> {
                builder.setContentText("Queued")
                    .setProgress(100, 0, true)
                    .setOngoing(false)
            }
            DownloadStatus.DOWNLOADING -> {
                if (state.totalBytes > 0) {
                    builder.setContentText("Downloading ${state.progress}%")
                        .setProgress(100, state.progress, false)
                } else {
                    builder.setContentText("Downloading...")
                        .setProgress(100, 0, true)
                }
                builder.setOngoing(true)
            }
            DownloadStatus.PAUSED -> {
                builder.setContentText("Paused at ${state.progress}%")
                    .setProgress(100, state.progress, false)
                    .setOngoing(false)
            }
            DownloadStatus.COMPLETED -> {
                builder.setContentText("Completed")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
                    .setAutoCancel(true)
            }
            DownloadStatus.FAILED -> {
                builder.setContentText(state.errorMessage ?: "Failed")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
            }
            DownloadStatus.CANCELED -> {
                builder.setContentText("Canceled")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
            }
        }

        manager.notify(state.id.hashCode(), builder.build())
    }

    /**
     * Removes a specific notification from the status bar.
     * @param id The unique task ID whose notification should be canceled.
     */
    fun cancelNotification(id: String) {
        manager.cancel(id.hashCode())
    }

    companion object {
        /** The ID of the notification channel used for download updates. */
        const val DOWNLOADER_NOTIFICATION_CHANNEL = "com.das.downloader.DOWNLOADER_NOTIFICATION_CHANNEL"
    }
}

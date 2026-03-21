package com.das.mediaHub.downloader

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.das.mediaHub.R
import com.das.mediaHub.data.constants.Notifications.DOWNLOADER_NOTIFICATION_CHANNEL
import com.das.mediaHub.data.model.download.DownloadState
import com.das.mediaHub.data.model.download.DownloadStatus

class DownloadNotifier(
    private val context: Context
) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun foregroundNotification(): Notification {
        return NotificationCompat.Builder(context, DOWNLOADER_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.download)
            .setContentTitle("MediaHub downloads")
            .setContentText("Managing downloads...")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

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

    fun cancelNotification(id: String) {
        manager.cancel(id.hashCode())
    }
}
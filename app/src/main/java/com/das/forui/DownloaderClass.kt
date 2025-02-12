package com.das.forui

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import java.io.File

class DownloaderClass(val context: Context) {



    fun downloadVideo(url: String, title: String, type: String) {
        try {
            val pathVideo = PathSaver().getVideosDownloadPath(context).toString()
            createSingleDirectory(pathVideo)

            createNotificationChannel()

            val builder = createMediaNotification(title)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = 26
            val uri = Uri.parse(url)


            val request = DownloadManager.Request(uri)
                .setTitle("Downloading Video")
                .setDescription("Video download in progress")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "$title.$type")
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverMetered(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)
            val query = DownloadManager.Query().setFilterById(downloadId)
            Thread {
                while (true) {
                    val cursor = downloadManager.query(query)
                    cursor?.let {
                        if (it.moveToFirst()) {
                            val bytesDownloadedIndex =
                                it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val bytesIndex =
                                it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                            if (bytesDownloadedIndex != -1 && bytesIndex != -1) {
                                val bytesDownloaded = it.getLong(bytesDownloadedIndex)
                                val bytesTotal = it.getLong(bytesIndex)

                                if (bytesTotal > 0) {
                                    val progress = (bytesDownloaded * 100L) / bytesTotal

                                    // Update notification with progress
                                    builder.setProgress(100, progress.toInt(), false)
                                    notificationManager.notify(notificationId, builder.build())

                                    if (bytesDownloaded == bytesTotal) {
                                        // When download is finished, show the completed notification
                                        builder.setContentText("Download complete")
                                            .setProgress(0, 0, false)
                                            .setOngoing(false)
                                        notificationManager.notify(notificationId, builder.build())
                                    }
                                }
                            }
                        }
                    }
                    cursor?.close()

                    Thread.sleep(1000)
                }
            }.start()

        } catch (e: Exception) {
            e.printStackTrace()
            createMediaNotification("Failed").setContentText("Found error ${e.message}").build()
            println("Download Failed: ${e.message}")
        }
    }


    private fun createSingleDirectory(directoryPath: String) {
        val dir = File(directoryPath)
        if (dir.mkdir()) {
        } else {
        }
    }

    private fun createMediaNotification(title: String): NotificationCompat.Builder {
        val deleteIntent = Intent(context, MainActivity::class.java)

        val deletePendingIntent = PendingIntent.getActivity(
            context,
            0,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val ourNotification = NotificationCompat.Builder(context, "downloadingChannel")
            .setContentTitle(title)
            .setContentText("Download in progress...")
            .setSmallIcon(R.drawable.music_note_24dp)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setProgress(100, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setSound(null)
            .setDeleteIntent(deletePendingIntent)
            .setVibrate(longArrayOf(0))

        return ourNotification

    }

    private fun createNotificationChannel() {
        // Only create the channel for Android 8.0 (API level 26) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "downloadingChannel"
            val channelName = "Downloading Process"
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableVibration(false)
                setShowBadge(false)
                description = "Channel notifications"
                enableLights(false)
                setSound(null, null)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
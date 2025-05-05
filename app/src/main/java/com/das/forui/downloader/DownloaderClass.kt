package com.das.forui.downloader

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import com.das.forui.R
import com.das.forui.databased.PathSaver.getAudioDownloadPath
import com.das.forui.databased.PathSaver.getVideosDownloadPath
import com.das.forui.objectsAndData.ForUIDataClass.DownloadData
import com.das.forui.objectsAndData.ForUIKeyWords.DOWNLOADER_NOTIFICATION_CHANNEL
import java.io.File

class DownloaderClass(val context: Context): ForUIDownloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    private val notificationManager = context.getSystemService(NotificationManager::class.java)





    override fun downloadVideo(url: String, title: String): Long {

        createNotificationChannel()
        val builder = createMediaNotificationForProgress(title)

        val uri = Uri.parse(url)
        val pathVideo = getVideosDownloadPath(context)
        createSingleDirectory(pathVideo)
        val customFilePath = File("$pathVideo/$title.mp4")

        if (customFilePath.parentFile?.exists()!!) {
            customFilePath.parentFile?.mkdirs()
        }
        val customUri = Uri.fromFile(customFilePath)
        val request = DownloadManager.Request(uri)
            .setTitle("Downloading Video")
            .setDescription("Video download in progress")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setDestinationUri(customUri)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setAllowedOverMetered(true)

        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (mimeType != null) {
            request.setMimeType(mimeType)
        }

        val downloadId = downloadManager.enqueue(request)
        startTacking(downloadId, builder)

        return downloadId

    }

    override fun downloadMusic(url: String, title: String): Long {
        createNotificationChannel()
        val builder = createMediaNotificationForProgress(title)

        val uri = Uri.parse(url)
        val pathVideo = getAudioDownloadPath(context)
        createSingleDirectory(pathVideo)
        val customFilePath = File("$pathVideo/$title.mp3")

        if (customFilePath.parentFile?.exists()!!) {
            customFilePath.parentFile?.mkdirs()
        }
        val customUri = Uri.fromFile(customFilePath)
        val request = DownloadManager.Request(uri)
            .setTitle("Downloading Music")
            .setDescription("Music download in progress")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setDestinationUri(customUri)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setAllowedOverMetered(true)

        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (mimeType != null) {
            request.setMimeType(mimeType)
        }

        val downloadId = downloadManager.enqueue(request)
        startTacking(downloadId, builder)
        return downloadId
    }


    override fun downloadVideosPlayList(url: String, playListName: String, title: String): Long {
        createNotificationChannel()
        val builder = createMediaNotificationForProgress(title)

        val uri = Uri.parse(url)
        val pathVideo = getVideosDownloadPath(context)
        createSingleDirectory(pathVideo)
        val customFilePath = File("$pathVideo/$playListName/$title.mp4")

        if (customFilePath.parentFile?.exists()!!) {
            customFilePath.parentFile?.mkdirs()
        }
        val customUri = Uri.fromFile(customFilePath)
        val request = DownloadManager.Request(uri)
            .setTitle("Downloading PlayList")
            .setDescription("Video download is in progress")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setDestinationUri(customUri)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setAllowedOverMetered(true)

        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (mimeType != null) {
            request.setMimeType(mimeType)
        }

        val downloadId = downloadManager.enqueue(request)
        startTacking(downloadId, builder)
        return downloadId
    }

    override fun downloadPlayListMusic(urls: List<DownloadData>): Long {
        createNotificationChannel()
        val pathVideo = getAudioDownloadPath(context)
        createSingleDirectory(pathVideo)
        for (i in urls) {
            val builder = createMediaNotificationForProgress(i.title)
            val uri = Uri.parse(i.url)
            val customFilePath = File("$pathVideo/${i.title}.mp3")

            if (customFilePath.parentFile?.exists()!!) {
                customFilePath.parentFile?.mkdirs()
            }

            val customUri = Uri.fromFile(customFilePath)
            val request = DownloadManager.Request(uri)
                .setTitle("Downloading Music")
                .setDescription("Music download in progress")
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setDestinationUri(customUri)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                .setAllowedOverMetered(true)


            val extension = MimeTypeMap.getFileExtensionFromUrl(i.url)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mimeType != null) {
                request.setMimeType(mimeType)
            }

            val downloadId = downloadManager.enqueue(request)
            startTacking(downloadId, builder)
            return downloadId
        }
        return 0L
    }

    private fun startTacking(downloadId: Long, builder: NotificationCompat.Builder) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val notificationId = downloadId.toInt()

        // Set initial notification state
        builder.setContentText("Preparing download...")
            .setProgress(0, 50, true)
            .setOngoing(true)
        notificationManager.notify(notificationId, builder.build())

        // Use a Handler to update UI periodically instead of blocking thread
        val handler = Handler(Looper.getMainLooper())
        val updateRunnable = object : Runnable {
            override fun run() {
                var cursor: Cursor? = null
                try {
                    cursor = downloadManager.query(query)

                    if (cursor != null && cursor.moveToFirst()) {
                        val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                        if (bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                            val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                            val bytesTotal = cursor.getLong(bytesTotalIndex)

                            if (bytesTotal > 0) {
                                val progress = (bytesDownloaded * 100L / bytesTotal).toInt()

                                builder.setContentText("Downloading...")
                                builder.setProgress(100, progress, false)
                                notificationManager.notify(notificationId, builder.build())

                                if (bytesDownloaded == bytesTotal) {
                                    builder.setContentText("Download complete")
                                        .setProgress(0, 0, false)
                                        .setOngoing(false)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                                    notificationManager.notify(notificationId, builder.build())

                                    // Remove the handler callback once download is complete
                                    handler.removeCallbacks(this)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    cursor?.close() // Make sure the cursor is closed once the operation is done
                }

                // Repeat the check after a delay
                handler.postDelayed(this, 1000)
            }
        }

        handler.post(updateRunnable)
    }

    private fun createSingleDirectory(directoryPath: String) {
        val dir = File(directoryPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }


    private fun createMediaNotificationForProgress(title: String): NotificationCompat.Builder {


        val ourNotification = NotificationCompat.Builder(context, DOWNLOADER_NOTIFICATION_CHANNEL)
            .setContentTitle(title)
            .setContentText("Download in progress...")
            .setSmallIcon(R.drawable.music_note_24dp)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setProgress(100, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setSound(null)
            .setVibrate(longArrayOf(0))

        return ourNotification

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = DOWNLOADER_NOTIFICATION_CHANNEL
            val channelName = "Media Downloader"
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableVibration(false)
                setShowBadge(false)
                description = "Media Downloader for videos and musics"
                enableLights(false)
                setSound(null, null)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }



}
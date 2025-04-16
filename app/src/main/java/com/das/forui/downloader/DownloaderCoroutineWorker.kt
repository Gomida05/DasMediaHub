package com.das.forui.downloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.das.forui.databased.PathSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloaderCoroutineWorker(
    private val context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params){




    override suspend fun doWork(): Result {
        val url = inputData.getString("file_url") ?: return Result.failure()
        val title = inputData.getString("title") ?: return Result.failure()


        val notificationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(1002, createForegroundInfo(0), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(1002, createForegroundInfo(0))
        }
        setForegroundAsync(notificationInfo)
        return try {
            downloadFileWithProgress(url, title)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }

    }

    private suspend fun downloadFileWithProgress(url: String, title: String) = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()

        val safeTitle = title.replace(Regex("[^a-zA-Z0-9 _\\-()]"), "_")
        val totalBytes = connection.contentLength
        val inputStream = connection.inputStream
        val outputFile = getUniqueFile(safeTitle, "mp4")

        val outputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(1024 * 4)
        var bytesRead: Int
        var downloadedBytes = 0
        var lastProgress = -1

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
            downloadedBytes += bytesRead

            val progress = (downloadedBytes * 100) / totalBytes
            if (progress != lastProgress) {
                createForegroundInfo(progress)
                lastProgress = progress
            }
        }

        outputStream.close()
        inputStream.close()
    }

    private fun getUniqueFile(baseName: String, extension: String): File {
        val videoPath = PathSaver.getVideosDownloadPath(context)
        var file = File(videoPath, "$baseName.$extension")
        var index = 1

        while (file.exists()) {
            file = File(videoPath, "$baseName($index).$extension")
            index++
        }

        return file

    }




    private fun createForegroundInfo(process: Int): Notification {
        val channelId = "download_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Download",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setSound(null, null)

            }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val title = inputData.getString("title")

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Downloading file")
            .setContentText(title)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, process, false)
            .setOngoing(true)
            .build()

        applicationContext.getSystemService(NotificationManager::class.java)
            .notify(1002, notification)

        return notification
    }



}
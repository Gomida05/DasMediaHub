package com.das.mediaHub.receive

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.das.mediaHub.MainActivity
import com.das.mediaHub.R
import com.das.mediaHub.data.constants.DownloadConstants
import com.das.mediaHub.data.constants.Notifications
import java.io.File

class ReceiverNotActive: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)

            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val expectedId = prefs.getLong(DownloadConstants.EXCEPTED_DOWNLOAD_ID, -1L)

            if (downloadId == expectedId) {

                val downloadManager = context.getSystemService<DownloadManager>()
                val notificationManager = context.getSystemService<NotificationManager>()
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager?.query(query)

                if (cursor != null && cursor.moveToFirst()) {
                    val uriString = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    val fileUri = uriString.toUri()
                    val apkFile = File(fileUri.path.toString())

                    val customIntent = Intent(context, MainActivity::class.java).apply {
                        action = DownloadConstants.DOWNLOAD_FINISHED
                        putExtra("apk_path", apkFile.absolutePath)
                    }

                    val mainPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        customIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val notification = NotificationCompat.Builder(context,
                        Notifications.DOWNLOADER_NOTIFICATION_CHANNEL
                    )
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Download complete")
                        .setContentText("Click here to install new version")
                        .setContentIntent(mainPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()

                    notificationManager?.notify(System.currentTimeMillis().toInt(), notification)
                }

                cursor?.close()
            }
        }
    }
}
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
import com.das.mediaHub.services.AudioServiceFromUrl
import com.das.mediaHub.services.BackGroundPlayer
import java.io.File

class Receiver: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            Notifications.AUDIO_SERVICE_FROM_URL_NOTIFICATION -> {
                context.stopService(
                    Intent(context, AudioServiceFromUrl::class.java)
                )
            }
            Notifications.BACKGROUND_GROUND_PLAYER_NOTIFICATION -> {
                context.stopService(Intent(context, BackGroundPlayer::class.java))
            }
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                context.downloadComplete(intent)
            }

        }
    }

    private fun Context.downloadComplete(intent: Intent) {
        val downloadManager = getSystemService<DownloadManager>()
        val notificationManager = getSystemService<NotificationManager>()

        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val expectedId = prefs.getLong(DownloadConstants.EXCEPTED_DOWNLOAD_ID, -1L)

        if (downloadId == expectedId) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager?.query(query)

            if (cursor != null && cursor.moveToFirst()) {
                val uriString = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                val fileUri = uriString.toUri()
                val apkFile = File(fileUri.path.toString())

                val customIntent = Intent(this, MainActivity::class.java).apply {
                    action = DownloadConstants.DOWNLOAD_FINISHED
                    putExtra("apk_path", apkFile.absolutePath)
                }

                val mainPendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    customIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(this,
                    Notifications.DOWNLOADER_NOTIFICATION_CHANNEL
                )
                    .setSmallIcon(R.mipmap.launcher_foreground)
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
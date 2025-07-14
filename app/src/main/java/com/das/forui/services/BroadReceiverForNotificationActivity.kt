package com.das.forui.services

import android.app.ActivityManager
import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.das.forui.R
import com.das.forui.data.constants.Notifications.AUDIO_SERVICE_FROM_URL_NOTIFICATION
import com.das.forui.data.constants.Notifications.BACKGROUND_GROUND_PLAYER_NOTIFICATION
import com.das.forui.data.constants.Notifications.DOWNLOADER_NOTIFICATION_CHANNEL
import com.das.forui.data.constants.DownloadConstants.EXCEPTED_DOWNLOAD_ID


class BroadReceiverForNotificationActivity: BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            AUDIO_SERVICE_FROM_URL_NOTIFICATION -> {
                context?.stopService(Intent(context, AudioServiceFromUrl::class.java))
            }
            BACKGROUND_GROUND_PLAYER_NOTIFICATION -> {
                context?.stopService(Intent(context, BackGroundPlayer::class.java))
            }
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                val prefs = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val expectedId = prefs?.getLong(EXCEPTED_DOWNLOAD_ID, -1L)

                if (id == expectedId) {
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                    val apkUri = downloadManager.getUriForDownloadedFile(id)


                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }

                    if (isAppInForeground(context)){
                        try {
                            context.startActivity(installIntent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No app found to open the APK file", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        } catch (e: Exception) {
                            Toast.makeText(context, "No app found to open the APK file", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                    else {
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            installIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        val notification = NotificationCompat.Builder(context, DOWNLOADER_NOTIFICATION_CHANNEL)
                            .setSmallIcon(R.drawable.download)
                            .setContentTitle("Update Ready")
                            .setContentText("Tap to install the update.")
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build()

                        val notificationManager = context.getSystemService(NotificationManager::class.java)
                        notificationManager.notify(1001, notification)
                    }
                }
            }
        }
    }


    fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }


}
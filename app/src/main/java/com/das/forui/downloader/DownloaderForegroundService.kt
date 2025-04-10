package com.das.forui.downloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.objectsAndData.ForUIKeyWords.DOWNLOADER_NOTIFICATION_CHANNEL

class DownloaderForegroundService: Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {




        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }




    private fun createNotificationBuilder(
        context: Context,
        title: String,
        contentText: String,
        isOnGoing: Boolean
    ): NotificationCompat.Builder {
        val deleteIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deletePendingIntent = if (isOnGoing) deleteIntent else Notification().deleteIntent



        val notificationBuilder = NotificationCompat.Builder(context, "downloadingChannel")
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.download)
            .setOngoing(isOnGoing)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setProgress(100, 0, true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setDeleteIntent(deletePendingIntent)

        return notificationBuilder

    }

    private fun createNotificationChannel() {
        // Only create the channel for Android 8.0 (API level 26) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = DOWNLOADER_NOTIFICATION_CHANNEL
            val channelName = "Download"
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
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }



}
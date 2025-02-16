package com.das.forui.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.das.forui.MainActivity
import com.das.forui.R

class MyService : Service() {
    private val channelId = "ForegroundServiceChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mainIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, mainIntent, PendingIntent.FLAG_MUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Service Running")
            .setContentText("Your service is running in the background")
            .setSmallIcon(R.mipmap.ic_launcher_ofme)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().setBigContentTitle("hello there").setSummaryText("this is for service that service is running in the background"))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)// This line makes it a heads-up notification
            .build()

        startForeground(1, notification)



        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(true)
                setSound(null, null)
                enableVibration(false)
                importance = NotificationManager.IMPORTANCE_HIGH
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}

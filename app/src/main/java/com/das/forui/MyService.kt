package com.das.forui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class MyService : Service() {
    private val channelId = "ForegroundServiceChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pendingIntent = PendingIntent.getActivity(
            application.applicationContext,
            0, intent, PendingIntent.FLAG_MUTABLE
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

        // Your service-related work here

        Thread {
//            val py = Python.getInstance()
//            val mainFile = py.getModule("main")
//            val variable = mainFile.callAttr("Service").toString()
//            Log.d("MainActivity", variable)
            while (true) {
                Log.d("MyService", "Service is running")
                Thread.sleep(1000)
            }
        }.start()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            serviceChannel.setShowBadge(true)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}

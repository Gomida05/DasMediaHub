package com.das.forui


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationBackgroundService: FirebaseMessagingService() {

    private val tAG = "MyFirebaseMsgService"


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        sendNotification(remoteMessage.toString())
        Log.d(tAG, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(tAG, "Message data payload: ${remoteMessage.data}")
        }

        // If the message contains a notification payload, handle it
        remoteMessage.notification?.let {
            Log.d(tAG, "Message Notification Body: ${it.body}")
        }
    }


    override fun onNewToken(token: String) {
        Log.d(tAG, "Refreshed token: $token")
    }
    private fun sendNotification(messageBody: String) {
        val channelId = "default_channel_id"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a NotificationChannel for Android 8.0 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "FCM Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("FCM Message")
            .setContentText(messageBody)
            .setSmallIcon(R.mipmap.icon)
            .build()

        // Display the notification
        notificationManager.notify(0, notification)
    }
}
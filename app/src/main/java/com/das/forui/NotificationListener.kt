package com.das.forui

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.das.forui.MediaPlayerNotificationManager.Companion.NOTIFICATION_ID
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class NotificationListener(
    private val musicService: MyMediaService
): PlayerNotificationManager.NotificationListener {

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)

        musicService.apply {
            stopForeground(true)
            isForeGroundService = false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        musicService.apply {
            if(ongoing && isForeGroundService){
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext, this::class.java)

                )
                startForeground(NOTIFICATION_ID, notification)
                isForeGroundService = true
            }
        }
    }

}
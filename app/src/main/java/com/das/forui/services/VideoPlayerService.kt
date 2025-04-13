package com.das.forui.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.session.MediaSession
import android.os.Build
import android.os.IBinder
import com.das.forui.MainActivity

class VideoPlayerService: Service() {


    override fun onCreate() {
        super.onCreate()
        createNotificationChannelForVideoPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        VideoMediaSession .VideoPlayerMediaSession.apply {
            isActive = true
            @Suppress("DEPRECATION")
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)

            @Suppress("DEPRECATION")
            setMediaButtonReceiver(
                PendingIntent.getBroadcast(
                    this@VideoPlayerService, 0,
                    Intent(Intent.ACTION_MEDIA_BUTTON),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
        }




        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun createNotificationChannelForVideoPlayer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "MEDIA_PLAYER",
                "Video Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                group = "MNGC"
                enableLights(false)
                enableVibration(false)
                setSound(null, null)


            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    object VideoMediaSession{
        val VideoPlayerMediaSession =  MediaSession(MainActivity(), "")
    }
}
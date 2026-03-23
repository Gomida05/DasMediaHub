package com.das.mediaHub.services.media

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.NotificationListener
import com.das.mediaHub.data.mediacontroller.MediaStoreCache
import com.das.mediaHub.data.mediacontroller.local.DescriptionAdapter

@SuppressLint("UnsafeOptInUsageError")
class LocalBackGroundPlayer: MediaSessionService() {


    private val mediaSession by lazy {
        MediaSession.Builder(this, player)
            .build()
    }
    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
    }


    private val playerNotificationManager by lazy {
        PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setMediaDescriptionAdapter(DescriptionAdapter(this))
            .setNotificationListener(notificationListener)
            .build()
            .apply {
                setPlayer(player)
                setMediaSessionToken(mediaSession.platformToken)
            }
    }


    override fun onCreate() {
        super.onCreate()
        val items: List<MediaItem> = MediaStoreCache.getMusics()
        if (items.isNotEmpty()) {
            player.setMediaItems(items)
            player.prepare()
        }
        playerNotificationManager
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val mediaId = intent?.getIntExtra("media_id", 0) ?: 0

        when (intent?.action) {
            ACTION_START -> {
                if (mediaId in 0 until player.mediaItemCount) {
                    player.seekTo(mediaId, 0)
                }
                player.play()
            }

            ACTION_PAUSE -> player.pause()
            ACTION_STOP -> {
                player.stop()
                stopSelf()
            }
        }

        return START_STICKY
    }



    private val notificationListener =
        object : NotificationListener {

            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                if (ongoing) {
                    startForeground(notificationId, notification)
                }
            }

            override fun onNotificationCancelled(
                notificationId: Int,
                dismissedByUser: Boolean
            ) {
                stopSelf()
            }
        }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.isPlaying) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        playerNotificationManager.setPlayer(null)
        player.release()
        mediaSession.release()
        super.onDestroy()
    }



    private companion object {
        const val CHANNEL_ID = "MusicPlayerNotification"
        const val NOTIFICATION_ID = 95
        const val ACTION_START = "com.das.mediaHub.START_BACKGROUND_MEDIA"
        const val ACTION_PAUSE = "com.das.mediaHub.PAUSE_BACKGROUND_MEDIA"
        const val ACTION_STOP = "com.das.mediaHub.STOP_BACKGROUND_MEDIA"
    }
}
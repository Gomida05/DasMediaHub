package com.das.mediaHub.services.media.local

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.das.mediaHub.data.mediacontroller.local.DescriptionAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A service that handles background audio playback for local media files.
 *
 * It manages an [ExoPlayer] instance and a [PlayerNotificationManager] to provide
 * playback controls in the system notification area. It supports basic playback
 * actions like START, PAUSE, and STOP via Intents.
 *
 * Example usage:
 * ```kotlin
 * val intent = Intent(context, LocalBackGroundPlayer::class.java).apply {
 *     action = "com.das.mediaHub.START_BACKGROUND_MEDIA"
 *     putExtra("media_id", trackIndex)
 * }
 * context.startService(intent)
 * ```
 */
@SuppressLint("UnsafeOptInUsageError")
@AndroidEntryPoint
class LocalBackGroundPlayer: MediaSessionService() {



    @Inject
    lateinit var player: ExoPlayer


    private var currentMediaId: Int? = null

    private val mediaSession by lazy {
        MediaSession.Builder(this, player)
            .setId(NOTIFICATION_ID.toString())
            .build()
    }


    private lateinit var playerNotificationManager: PlayerNotificationManager


    override fun onCreate() {
        super.onCreate()
        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setMediaDescriptionAdapter(DescriptionAdapter(this))
            .setNotificationListener(notificationListener)
            .build()
            .apply {
                setPlayer(player)
                setMediaSessionToken(
                    mediaSession.platformToken
                )
            }
        player.prepare()
        playerNotificationManager

    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val mediaId = intent?.getIntExtra("media_id", 0) ?: 0

        if (mediaId == currentMediaId && player.isPlaying) return START_STICKY
        if (mediaId == currentMediaId && !player.isPlaying) {
            player.play()
            return START_STICKY
        }
        currentMediaId = mediaId

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
        object : PlayerNotificationManager.NotificationListener {

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
        player.clearMediaItems()

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
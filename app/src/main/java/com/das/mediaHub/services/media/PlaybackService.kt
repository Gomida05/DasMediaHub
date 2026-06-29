package com.das.mediaHub.services.media

import android.content.Intent
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.das.mediaHub.data.mediacontroller.online.VideoPlayerManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A service that hosts the [MediaSession] for background media playback.
 *
 * It allows the app to continue playing audio or video even when the UI is not visible
 * and integrates with system-level media controls (notification, lock screen, Bluetooth devices).
 *
 * This service is powered by Media3 and utilizes [VideoPlayerManager] for actual playback control.
 *
 * Example usage:
 * This service is usually started automatically by the [MediaController] or when
 * [VideoPlayerManager] begins playback.
 * ```kotlin
 * // It is declared in AndroidManifest.xml and bound via MediaSession
 * ```
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var player: VideoPlayerManager

    @Inject
    lateinit var mediaSession : MediaSession

    override fun onCreate() {
        super.onCreate()
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val exoPlayer = player.player
        if (!exoPlayer.isPlaying) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession.release()
        player.closeCurrentlyMedia()
        super.onDestroy()
    }
}
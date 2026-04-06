package com.das.mediaHub.services.media

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
    }
    private val mediaSession by lazy {
        MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    fun setPlaylist(items: List<MediaItem>, startIndex: Int = 0, playWhenReady: Boolean = true) {
        val exoPlayer = player
        exoPlayer.setMediaItems(items, startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = playWhenReady
    }

    fun playSingle(item: MediaItem) {
        val exoPlayer = player
        exoPlayer.setMediaItem(item)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val exoPlayer = player
        if (!exoPlayer.isPlaying) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }
}
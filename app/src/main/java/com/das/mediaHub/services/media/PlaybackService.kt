package com.das.mediaHub.services.media

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.das.mediaHub.MainApplication

class PlaybackService : MediaSessionService() {

    private val app by lazy {
        this.application as MainApplication
    }
    private val player by lazy {
        app.videoPlayerMainApplication
    }
    private val mediaSession by lazy {
        MediaSession.Builder(this, player.player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    fun setPlaylist(items: List<MediaItem>, startIndex: Int = 0, playWhenReady: Boolean = true) {
        val exoPlayer = player.player
        exoPlayer.setMediaItems(items, startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = playWhenReady
    }

    fun playSingle(item: MediaItem) {
        val exoPlayer = player.player
        exoPlayer.setMediaItem(item)
        exoPlayer.prepare()
        exoPlayer.play()
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
        player.release()
        super.onDestroy()
    }
}
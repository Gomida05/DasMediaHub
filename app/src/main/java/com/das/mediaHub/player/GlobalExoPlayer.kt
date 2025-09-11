package com.das.mediaHub.player

import android.content.Context
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.das.mediaHub.player.video.PlayerController
import kotlin.io.path.Path

object GlobalExoPlayer: PlayerController {


    override var player: ExoPlayer? = null
    var mediaSession: MediaSession? = null
    var myMediaSession: MediaSessionCompat? = null

    val playerHasItem = player?.currentMediaItem  == null

    fun getPlayer(context: Context): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(AUDIO_CONTENT_TYPE_MOVIE)
                        .build(), true
                )
                .setHandleAudioBecomingNoisy(true)
                .build()
            mediaSession = MediaSession.Builder(context, player!!).build()
        }
        return player!!
    }

    private fun addMediaItem(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)

        player?.setMediaItem(mediaItem)
    }

    override fun playVideo(url: Uri) {

        player?.let {
            if (it.currentMediaItem != null) {
                it.prepare()
                addListener()
                it.play()
            } else {
                addMediaItem(url)
                it.prepare()
                addListener()
                it.play()
            }
        }
    }

    override fun addListener() {
//        player?.addListener()
    }

    override fun pause() {
        player?.pause()
    }

    override fun resume() {
        player?.play()
    }

    override fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    override fun release() {
        player?.release()
        player = null
    }
}
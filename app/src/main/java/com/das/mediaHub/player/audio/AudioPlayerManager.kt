package com.das.mediaHub.player.audio

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class AudioPlayerManager(
    private val context: Context
) {

    val player by lazy {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(AUDIO_CONTENT_TYPE_MOVIE)
                    .build(), true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    val isEmptyMediaItem = player.currentMediaItem == null

    fun addListener() {
//        player.addListener(
////            MyExoPlayerCallBack(title, channelName)
//        )
    }

    fun playVideo(item: MediaItem) {
        if (item == player.currentMediaItem) {
            player.play()
        } else {
            player.setMediaItem(item)
            player.prepare()
            player.play()
        }
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun release() {
        player.release()
    }
}
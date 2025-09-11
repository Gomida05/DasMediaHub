package com.das.mediaHub.player.video

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.das.mediaHub.mediacontroller.LocalVideoListener

class LocalVideoManger(
    private val context: Context,
    private val playerListener: LocalVideoListener
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

    val isEmptyMediaItem = player.currentMediaItem  == null

    fun addListener() {
        player.addListener(
            playerListener
        )
    }

    private fun addMediaItem(item: MediaItem) {
        player.setMediaItem(item)
    }

    fun addMediaItems(mediaItems: List<MediaItem>) {
        player.addMediaItems(mediaItems)
    }

    fun playVideo(url: MediaItem) {

        if (player.currentMediaItem != null) {
            player.prepare()
            addListener()
            player.play()
        } else {
            addMediaItem(url)
            player.prepare()
            addListener()
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
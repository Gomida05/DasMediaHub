package com.das.mediaHub.player.video

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.das.mediaHub.MainActivity
import com.das.mediaHub.PIP.shouldEnterPipMode
import com.das.mediaHub.WakeLockHelper.acquireWakeLock
import com.das.mediaHub.WakeLockHelper.releaseWakeLock

class LocalVideoManger(private val mainActivity: MainActivity) {

    val context = mainActivity
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

    fun setPlaylist(items: List<MediaItem>) {
        player.setMediaItems(/* mediaItems = */ items, /* resetPosition = */ false)
        player.prepare()
    }

    fun preloadNext() {
        val player = player
        val nextIndex = player.currentMediaItemIndex + 1

        if (nextIndex < player.mediaItemCount) {
            player.seekTo(nextIndex, 0)
            player.pause()
            player.seekTo(player.currentMediaItemIndex, player.currentPosition)
        }
    }

    fun addListener() {
        player.addListener(
            LocalVideoListener()
        )
    }

    private fun addMediaItem(item: MediaItem) {
        player.setMediaItem(item)
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


    fun release() {
        player.release()
    }

    inner class LocalVideoListener: Player.Listener {

        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_READY) {
                preloadNext()
            }
        }
        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            shouldEnterPipMode.value = false
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            shouldEnterPipMode.value = isPlaying
            if (isPlaying) {
                mainActivity.acquireWakeLock()
            } else {
                mainActivity.releaseWakeLock()
            }
        }
    }
}
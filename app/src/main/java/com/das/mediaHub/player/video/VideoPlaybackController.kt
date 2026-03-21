package com.das.mediaHub.player.video

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.das.mediaHub.PIP.canEnterPipMode

class VideoPlaybackController(
    private val player: Player
) {

    private var listenerAttached = false

    fun attachListener() {
        if (listenerAttached) return
        player.addListener(videoListener)
        listenerAttached = true
    }

    fun playSingle(item: MediaItem) {
        player.setMediaItem(item)
        player.prepare()
        player.play()
    }

    fun playPlaylist(
        items: List<MediaItem>,
        startIndex: Int = 0
    ) {
        if (items.isEmpty()) return
        player.setMediaItems(items, startIndex, 0L)
        player.prepare()
        player.play()
    }

    fun setPlaylist(
        items: List<MediaItem>,
        startIndex: Int = 0
    ) {
        if (items.isEmpty()) return
        player.setMediaItems(items, startIndex, 0L)
        player.prepare()
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun preloadNext() {
        val nextIndex = player.currentMediaItemIndex + 1
        if (nextIndex >= player.mediaItemCount) return

        val currentIndex = player.currentMediaItemIndex
        val currentPosition = player.currentPosition

        player.seekTo(nextIndex, 0)
        player.pause()
        player.seekTo(currentIndex, currentPosition)
    }

    private val videoListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_READY) {
                preloadNext()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            canEnterPipMode.value = false
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            canEnterPipMode.value = isPlaying
        }
    }
}
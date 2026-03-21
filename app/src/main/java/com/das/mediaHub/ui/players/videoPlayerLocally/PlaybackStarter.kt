package com.das.mediaHub.ui.players.videoPlayerLocally

import androidx.media3.common.MediaItem

object PlaybackStarter {

    fun playPlaylist(
        controller: androidx.media3.session.MediaController,
        items: List<MediaItem>,
        startIndex: Int
    ) {
        controller.setMediaItems(items, startIndex, 0L)
        controller.prepare()
        controller.play()
    }

    fun playSingle(
        controller: androidx.media3.session.MediaController,
        item: MediaItem
    ) {
        controller.setMediaItem(item)
        controller.prepare()
        controller.play()
    }
}
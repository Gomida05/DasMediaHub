package com.das.mediaHub.ui.players.videoPlayer.state

import com.das.python.data.model.searcher.Video

data class VideoUiState(
    val title: String? = null,
    val duration: String? = null,
    val views: String? = null,
    val date: String? = null,
    val channelName: String? = null,
    val channelThumbnail: String? = null
) {
    companion object {
        fun Video.toVideoUiState(): VideoUiState {
            return VideoUiState(
                title = title,
                duration = duration,
                views = viewCount?.short,
                date = publishedTime,
                channelName = channel?.name,
                channelThumbnail = channel?.thumbnails?.firstOrNull()?.url
            )
        }
    }
}

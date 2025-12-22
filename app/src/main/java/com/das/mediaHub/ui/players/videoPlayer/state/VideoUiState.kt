package com.das.mediaHub.ui.players.videoPlayer.state

import com.das.mediaHub.data.model.searcher.Video

data class VideoUiState(
    val title: String? = null,
    val duration: String? = null,
    val views: String? = null,
    val date: String? = null,
    val channelName: String? = null,
    val channelThumbnail: String? = null
) {
    companion object {
        fun from(video: Video): VideoUiState {
            return VideoUiState(
                title = video.title,
                duration = video.duration,
                views = video.viewCount?.short,
                date = video.publishedTime,
                channelName = video.channel?.name,
                channelThumbnail = video.channel?.thumbnails?.firstOrNull()?.url
            )
        }
    }
}

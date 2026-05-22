package com.das.mediaHub.data.model.state

import com.das.python.data.model.searcher.Video

/**
 * Data class representing the state of video metadata displayed in the UI.
 *
 * @property title Video title.
 * @property duration Formatted video duration.
 * @property views Formatted view count.
 * @property date Formatted upload date.
 * @property channelName Name of the channel.
 * @property channelThumbnail URL of the channel's profile picture.
 */
data class VideoUiState(
    val title: String? = null,
    val duration: String? = null,
    val views: String? = null,
    val date: String? = null,
    val channelName: String? = null,
    val channelThumbnail: String? = null
) {
    companion object {
        /**
         * Extension function to convert a [Video] domain model to [VideoUiState].
         */
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

        /** Default empty state. */
        val EMPTY = VideoUiState()
    }
}

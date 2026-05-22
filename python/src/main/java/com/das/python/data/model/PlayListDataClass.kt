package com.das.python.data.model

import kotlinx.serialization.Serializable

/**
 * Data class representing an item within a YouTube playlist.
 *
 * @property url Full YouTube URL of the video.
 * @property title Video title.
 * @property views Formatted view count.
 * @property date Relative upload date.
 * @property duration Formatted video duration.
 */
@Serializable
data class PlayListDataClass(
    val url: String,
    val title: String,
    val views: String,
    val date: String,
    val duration: String
)

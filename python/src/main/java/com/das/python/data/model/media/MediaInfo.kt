package com.das.python.data.model.media

import kotlinx.serialization.Serializable

/**
 * Data class representing metadata for a [MediaInfo] video.
 *
 * @property id Unique MediaInfo video ID.
 * @property title Title or description of the video.
 * @property duration Video duration in seconds.
 * @property thumbnail URL of the video's thumbnail.
 * @property uploader Username of the uploader.
 * @property view_count Total views.
 * @property like_count Total likes.
 * @property webpage_url Full URL to the MediaInfo video page.
 * @property stream_url Direct streaming URL for the video.
 */
@Serializable
data class MediaInfo(
    val id: String?,
    val title: String?,
    val duration: String?,
    val thumbnail: String?,
    val uploader: String?,
    val view_count: String?,
    val like_count: String?,
    val webpage_url: String?,
    val stream_url: StreamInfo?
)
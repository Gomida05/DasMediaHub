package com.das.mediaHub.data.model.tiktok

import kotlinx.serialization.Serializable

/**
 * Data class representing metadata for a TikTok video.
 *
 * @property id Unique TikTok video ID.
 * @property title Title or description of the video.
 * @property duration Video duration in seconds.
 * @property thumbnail URL of the video's thumbnail.
 * @property uploader Username of the uploader.
 * @property view_count Total views.
 * @property like_count Total likes.
 * @property webpage_url Full URL to the TikTok video page.
 * @property stream_url Direct streaming URL for the video.
 */
@Serializable
data class TikTokInfo(
    val id: String?,
    val title: String?,
    val duration: Int?,
    val thumbnail: String?,
    val uploader: String?,
    val view_count: Long?,
    val like_count: Long?,
    val webpage_url: String?,
    val stream_url: String?
)

package com.das.python.data.model

import kotlinx.serialization.Serializable

/**
 * Data class representing basic metadata for a YouTube video
 * typically returned in search results or playlist listings.
 *
 * @property videoId Unique 11-character YouTube video identifier.
 * @property title Video title.
 * @property views Formatted view count (e.g., "1.2M views").
 * @property dateOfVideo Relative upload date (e.g., "2 years ago").
 * @property duration Video length (e.g., "3:45").
 * @property channelName Name of the uploading channel.
 * @property channelThumbnailsUrl URL to the channel's profile picture.
 */
@Serializable
data class VideosListData(
    val videoId: String,
    val title: String,
    val views: String,
    val dateOfVideo: String,
    val duration: String,
    val channelName: String,
    val channelThumbnailsUrl: String
)

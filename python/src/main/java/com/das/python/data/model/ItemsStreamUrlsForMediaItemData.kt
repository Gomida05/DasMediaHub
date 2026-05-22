package com.das.python.data.model

/**
 * Data class combining a direct audio stream URL with video metadata.
 *
 * @property audioUrl Direct URL to the audio stream.
 * @property videoId Unique YouTube video ID.
 * @property title Video title.
 * @property views Formatted view count.
 * @property dateOfVideo Relative upload date.
 * @property duration Video length.
 * @property channelName Channel name.
 * @property channelThumbnailsUrl URL to the channel thumbnail.
 */
data class ItemsStreamUrlsForMediaItemData(
    val audioUrl: String,
    val videoId: String,
    val title: String,
    val views: String,
    val dateOfVideo: String,
    val duration: String,
    val channelName: String,
    val channelThumbnailsUrl: String
)

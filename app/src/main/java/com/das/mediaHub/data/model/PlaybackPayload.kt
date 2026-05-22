package com.das.mediaHub.data.model

/**
 * Data class representing the payload required to initiate video playback.
 *
 * This includes all necessary metadata to display the video information 
 * in the player and background notifications.
 *
 * @property videoId Unique identifier for the video.
 * @property mediaUrl The direct streaming URL of the media content.
 * @property title Title of the video.
 * @property channelName Name of the channel that uploaded the video.
 * @property views Formatted view count.
 * @property date Formatted upload date.
 * @property duration Formatted video duration.
 *
 * Example usage:
 * ```kotlin
 * val payload = PlaybackPayload(
 *     videoId = "123",
 *     mediaUrl = "https://...",
 *     title = "Epic Video",
 *     // ... other fields
 * )
 * ```
 */
data class PlaybackPayload(
    val videoId: String,
    val mediaUrl: String,
    val title: String,
    val channelName: String,
    val views: String,
    val date: String,
    val duration: String
)

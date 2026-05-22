package com.das.mediaHub.data.model.state

/**
 * Data class representing the state of a video stream URL for a specific video.
 *
 * @property videoId The unique ID of the video.
 * @property url The resolved streaming URL for the video.
 */
data class VideoStreamState(
    val videoId: String,
    val url: String
)

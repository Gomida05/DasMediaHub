package com.das.downloader.data.model.download

import kotlinx.serialization.Serializable

/**
 * Enum defining the types of media or files being downloaded and their 
 * associated file extensions.
 * 
 * @property extension The standard file extension for this type.
 */
@Serializable
enum class DownloadType(val extension: String) {
    /** Standard MP4 video file. */
    YOUTUBE_VIDEO(".mp4"),
    /** Standard MP3 audio file. */
    YOUTUBE_AUDIO(".mp3"),

    /** Specialized marker for SocialMedia videos that have the stream url. */
    SOCIAL_MEDIA_VIDEO(".mp4")
}

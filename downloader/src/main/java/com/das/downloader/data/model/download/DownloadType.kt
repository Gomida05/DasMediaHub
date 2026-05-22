package com.das.downloader.data.model.download

/**
 * Enum defining the types of media or files being downloaded and their 
 * associated file extensions.
 * 
 * @property extension The standard file extension for this type.
 */
enum class DownloadType(val extension: String) {
    /** Standard MP4 video file. */
    VIDEO(".mp4"),
    /** Standard MP3 audio file. */
    MUSIC(".mp3"),
    /** Specialized marker for TikTok videos. */
    TIKTOK_VIDEO("tiktok"),
    /** Android package file. */
    APK(".apk")
}

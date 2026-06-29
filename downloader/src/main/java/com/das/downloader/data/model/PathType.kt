package com.das.downloader.data.model

/**
 * Enum representing the different types of storage paths used by the downloader.
 * 
 * @property label The key used to store this path in [com.das.downloader.data.local.DownloadPreferences].
 */
enum class PathType(val label: String) {
    /** Path where audio files are saved. */
    AUDIO(label = "download_path1"),
    /** Path where video files are saved. */
    VIDEO(label = "download_path2")
}

package com.das.mediaHub.data.mediacontroller

import androidx.media3.common.MediaItem

/**
 * Singleton object used to cache lists of [MediaItem]s scanned from the local device.
 *
 * This helps avoid repeated and expensive MediaStore scans when navigating 
 * between different local media screens.
 *
 * Example usage:
 * ```kotlin
 * MediaStoreCache.updateMusicFiles(scannedList)
 * val music = MediaStoreCache.getMusics()
 * ```
 */
internal object MediaStoreCache {

    @Volatile
    private var cachedMusicItems: List<MediaItem> = emptyList()

    @Volatile
    private var cachedVideosItems: List<MediaItem> = emptyList()

    /**
     * Updates the cached list of music items.
     * @param items The new list of media items.
     */
    fun updateMusicFiles(items: List<MediaItem>) {
        cachedMusicItems = items
    }

    /**
     * Updates the cached list of video items.
     * @param items The new list of media items.
     */
    fun updateVideosFiles(items: List<MediaItem>) {
        cachedVideosItems = items
    }

    /**
     * Retrieves the current cached list of music items.
     * @return A list of media items.
     */
    fun getMusics(): List<MediaItem> = cachedMusicItems

    /**
     * Retrieves the current cached list of video items.
     * @return A list of media items.
     */
    fun getVideos(): List<MediaItem> = cachedVideosItems
}

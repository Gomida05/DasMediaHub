package com.das.mediaHub.data.media

import androidx.media3.common.MediaItem

internal object MediaStoreCache {

    @Volatile
    private var cachedMusicItems: List<MediaItem> = emptyList()

    @Volatile
    private var cachedVideosItems: List<MediaItem> = emptyList()

    fun updateMusicFiles(items: List<MediaItem>) {
        cachedMusicItems = items
    }

    fun updateVideosFiles(items: List<MediaItem>) {
        cachedVideosItems = items
    }

    fun getMusics(): List<MediaItem> = cachedMusicItems

    fun getVideos(): List<MediaItem> = cachedVideosItems
}
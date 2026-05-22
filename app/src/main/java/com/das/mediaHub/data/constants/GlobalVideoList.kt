package com.das.mediaHub.data.constants

import com.das.python.data.model.searcher.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global in-memory store for video items that can be shared across screens.
 *
 * This object exposes a read-only [StateFlow] to observers and provides helper
 * methods to update the list safely while preventing duplicate videos by ID.
 * It is commonly used to store search results or playlist items that need to 
 * be accessible from multiple UI components without re-fetching.
 *
 * Example usage:
 * ```kotlin
 * // In a Composable or ViewModel
 * GlobalVideoList.videos.collect { currentList ->
 *     // Update UI with the shared list
 * }
 * 
 * // Adding new results
 * GlobalVideoList.addVideos(newSearchResults)
 * ```
 */
object GlobalVideoList {

    /**
     * Backing state that holds the current list of videos.
     */
    private val _videos = MutableStateFlow<List<Video>>(emptyList())

    /**
     * Public read-only stream of the current video list.
     */
    val videos = _videos.asStateFlow()

    /**
     * Replaces the current list with [items].
     *
     * Duplicate videos are removed based on their [Video.id].
     *
     * @param items the new list of videos to store
     */
    fun setVideos(items: List<Video>) {
        _videos.value = items.distinctBy { it.id }
    }

    /**
     * Adds a single [video] to the current list.
     *
     * If another video with the same [Video.id] already exists,
     * only one instance is kept.
     *
     * @param video the video to add
     */
    fun addVideo(video: Video) {
        _videos.value = (_videos.value + video).distinctBy { it.id }
    }

    /**
     * Adds multiple [items] to the current list.
     *
     * Duplicate videos are removed based on their [Video.id].
     *
     * @param items the videos to add
     */
    fun addVideos(items: List<Video>) {
        _videos.value = (_videos.value + items).distinctBy { it.id }
    }

    /**
     * Removes a video from the current list by its ID.
     *
     * @param videoId the ID of the video to remove
     */
    fun removeVideo(videoId: String) {
        _videos.value = _videos.value.filterNot { it.id == videoId }
    }

    /**
     * Clears all videos from the store.
     */
    fun clear() {
        _videos.value = emptyList()
    }

    /**
     * Returns the video at the given [index], or null if the index is out of bounds.
     *
     * @param index the position of the requested video
     * @return the video at [index], or null when unavailable
     */
    fun getVideoAt(index: Int): Video? {
        return _videos.value.getOrNull(index)
    }

    /**
     * Returns the video by the given [videoId], or null if the ID is out of bounds.
     *
     * @param videoId the ID of the requested video
     * @return the video by [videoId], or null when unavailable
     */
    fun getVideoById(videoId: String): Video? {
        return _videos.value.find { it.id == videoId }
    }
}

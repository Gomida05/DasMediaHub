package com.das.mediaHub.data.constants

import com.das.python.data.model.searcher.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global in-memory store for video items that can be shared across screens.
 *
 * This object exposes a read-only [StateFlow] to observers and provides helper
 * methods to update the list safely while preventing duplicate videos by id.
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
     * Removes a video from the current list by its id.
     *
     * @param videoId the id of the video to remove
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
}
package com.das.mediaHub.data.repository

import com.das.mediaHub.data.local.db.dao.WatchHistoryDao
import com.das.mediaHub.data.model.WatchedVideoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository responsible for managing the user's video watch history.
 *
 * It provides an abstraction layer over [WatchHistoryDao] to handle adding, 
 * retrieving, and clearing history entries.
 *
 * Example usage:
 * ```kotlin
 * @Inject lateinit var repository: WatchHistoryRepository
 * repository.insertNewVideo(videoId = "123", title = "Another Video", ...)
 * ```
 */
@Singleton
class WatchHistoryRepository @Inject constructor(
    private val dao: WatchHistoryDao
) {

    /**
     * Retrieves all videos in the watch history ordered by most recent.
     * @return A [Flow] emitting the list of watched videos.
     */
    fun getWatchedVideos(): Flow<List<WatchedVideoEntity>> = dao.getWatchedVideos()

    /**
     * Inserts a new video into the history. If the history limit is reached, 
     * the oldest entries are removed automatically.
     * 
     * @param videoId Unique video identifier.
     * @param title Video title.
     * @param videoDate Upload date.
     * @param videoViewCount Number of views.
     * @param videoChannelName Channel name.
     * @param duration Video length.
     * @param channelThumbnail URL of the channel's profile picture.
     */
    suspend fun insertNewVideo(
        videoId: String,
        title: String,
        videoDate: String,
        videoViewCount: String,
        videoChannelName: String,
        duration: String,
        channelThumbnail: String
    ) {
        dao.insertWithLimit(
            WatchedVideoEntity(
                title = title,
                videoId = videoId,
                views = videoViewCount,
                dateTime = videoDate,
                duration = duration,
                channelName = videoChannelName,
                channelThumbnail = channelThumbnail
            )
        )
    }

    /**
     * Deletes a specific video entry from the watch history.
     * @param videoId The ID to delete.
     * @return Number of rows deleted.
     */
    suspend fun deleteWatchUrl(videoId: String): Int {
        return dao.deleteWatchUrl(videoId)
    }

    /**
     * Clears the entire watch history.
     */
    suspend fun clearAll() {
        dao.clearAll()
    }
}

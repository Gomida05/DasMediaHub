package com.das.mediaHub.data.repository

import com.das.mediaHub.data.local.db.dao.FavoritesDao
import com.das.mediaHub.data.model.SavedVideosListData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository responsible for managing videos saved to the user's favorites (Saved for Later).
 *
 * It provides an abstraction layer over [FavoritesDao] for performing CRUD 
 * operations on saved videos.
 *
 * Example usage:
 * ```kotlin
 * @Inject lateinit var repository: FavoritesRepository
 * repository.insertData(videoId = "123", title = "Sample Video", ...)
 * ```
 */
@Singleton
class FavoritesRepository @Inject constructor(
    private val dao: FavoritesDao
) {

    /**
     * A [Flow] of all videos currently saved in the favorites database.
     */
    val allSavedVideos: Flow<List<SavedVideosListData>>
        get() = dao.getAllSavedVideos()

    /**
     * Checks if a specific video ID exists in the favorites.
     * @param videoId The ID to check.
     * @return A [Flow] emitting true if it exists, false otherwise.
     */
    fun isWatchUrlExist(videoId: String): Flow<Boolean> =
        dao.isWatchUrlExist(videoId)

    /**
     * Inserts a new video into the favorites database.
     * 
     * @param videoId Unique video identifier.
     * @param title Video title.
     * @param videoDate Upload date.
     * @param videoViewCount Number of views.
     * @param videoChannelName Channel name.
     * @param duration Video length.
     * @param channelThumbnail URL of the channel's profile picture.
     */
    suspend fun insertData(
        videoId: String,
        title: String,
        videoDate: String,
        videoViewCount: String,
        videoChannelName: String,
        duration: String,
        channelThumbnail: String
    ) {
        dao.insert(
            SavedVideosListData(
                videoId = videoId,
                title = title,
                views = videoViewCount,
                dateTime = videoDate,
                channelName = videoChannelName,
                duration = duration,
                channelThumbnail = channelThumbnail
            )
        )
    }

    /**
     * Removes a video from the favorites database by its ID.
     * @param videoId The ID to remove.
     * @return Number of rows deleted.
     */
    suspend fun deleteWatchUrl(videoId: String): Int =
        dao.deleteWatchUrl(videoId)

    /**
     * Clears all entries from the favorites database.
     */
    suspend fun clearAll() =
        dao.clearAll()
}

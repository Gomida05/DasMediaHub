package com.das.mediaHub.data.local.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.das.mediaHub.data.model.SavedVideosListData
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the "Saved_for_later" table.
 *
 * Provides methods to manage videos that users have explicitly saved for later viewing.
 */
@Dao
interface FavoritesDao {

    /**
     * Inserts a video into favorites. Ignores if the video already exists.
     * @return Row ID of the inserted item, or -1 if ignored.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(video: SavedVideosListData): Long

    /**
     * Retrieves all saved videos, ordered by the most recently added.
     */
    @Query("SELECT * FROM Saved_for_later ORDER BY rowid DESC")
    fun getAllSavedVideos(): Flow<List<SavedVideosListData>>

    /**
     * Checks if a video ID is present in the favorites table.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM Saved_for_later WHERE video_id = :videoId)")
    fun isWatchUrlExist(videoId: String): Flow<Boolean>

    /**
     * Deletes a specific video from favorites.
     * @return Number of rows deleted.
     */
    @Query("DELETE FROM Saved_for_later WHERE video_id = :videoId")
    suspend fun deleteWatchUrl(videoId: String): Int

    /**
     * Removes all videos from the favorites table.
     */
    @Query("DELETE FROM Saved_for_later")
    suspend fun clearAll()
}

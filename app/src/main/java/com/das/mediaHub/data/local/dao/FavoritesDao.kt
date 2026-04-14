package com.das.mediaHub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.das.mediaHub.data.model.SavedVideosListData
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(video: SavedVideosListData): Long

    @Query("SELECT * FROM Saved_for_later ORDER BY rowid DESC")
    suspend fun getAllSavedVideos(): List<SavedVideosListData>

    @Query("SELECT EXISTS(SELECT 1 FROM Saved_for_later WHERE video_id = :videoId)")
    fun isWatchUrlExist(videoId: String): Flow<Boolean>

    @Query("DELETE FROM Saved_for_later WHERE video_id = :videoId")
    suspend fun deleteWatchUrl(videoId: String): Int

    @Query("DELETE FROM Saved_for_later")
    suspend fun clearAll()
}
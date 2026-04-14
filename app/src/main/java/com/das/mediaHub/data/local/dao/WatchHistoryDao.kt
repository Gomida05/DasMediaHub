package com.das.mediaHub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.das.mediaHub.data.model.WatchedVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(video: WatchedVideoEntity): Long

    @Query("""
        SELECT * FROM Watched_Videos
        ORDER BY rowid DESC
    """)
    suspend fun getWatchedVideos(): List<WatchedVideoEntity>

    @Query("""
        DELETE FROM Watched_Videos
        WHERE rowid IN (
            SELECT rowid FROM Watched_Videos
            ORDER BY rowid ASC
            LIMIT 1
        )
    """)
    suspend fun deleteOldest()

    @Query("SELECT COUNT(*) FROM Watched_Videos")
    suspend fun getCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM Watched_Videos WHERE video_id = :videoId)")
    suspend fun exists(videoId: String): Boolean

    @Query("DELETE FROM Watched_Videos WHERE video_id = :videoId")
    suspend fun deleteWatchUrl(videoId: String): Int

    @Query("DELETE FROM Watched_Videos")
    suspend fun clearAll()
}
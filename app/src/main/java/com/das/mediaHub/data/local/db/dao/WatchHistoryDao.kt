package com.das.mediaHub.data.local.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.das.mediaHub.data.model.WatchedVideoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the "Watched_Videos" table.
 *
 * It manages the user's watch history, including an automated limit to ensure 
 * the history doesn't grow indefinitely.
 */
@Dao
interface WatchHistoryDao {

    /**
     * Inserts a video into history and automatically maintains a limit of 30 entries.
     * Oldest entries are deleted if the limit is exceeded.
     */
    @Transaction
    suspend fun insertWithLimit(video: WatchedVideoEntity) {
        val inserted = insert(video)
        if (inserted == -1L) return
        if (getCount() > 30) {
            deleteOldest()
        }
    }

    /**
     * Inserts a video entity. Ignores if the video ID already exists.
     * @return Row ID of inserted item.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(video: WatchedVideoEntity): Long

    /**
     * Retrieves all watched videos, ordered by most recent (asc logic in code seems to be oldest first based on rowid, 
     * but usually UI reverses it or rowid desc is used. Assuming rowid ASC here means oldest first).
     */
    @Query("""
        SELECT * FROM Watched_Videos
        ORDER BY rowid ASC
    """)
    fun getWatchedVideos(): Flow<List<WatchedVideoEntity>>

    /**
     * Deletes the oldest entries that exceed the 30-item limit.
     */
    @Query("""       
        DELETE FROM Watched_Videos
        WHERE rowid IN (
              SELECT rowid FROM Watched_Videos
              ORDER BY rowid ASC
              LIMIT (SELECT COUNT(*) - 30 FROM Watched_Videos)
        )
    """)
    suspend fun deleteOldest()

    /**
     * Returns the total number of entries in the watch history.
     */
    @Query("SELECT COUNT(*) FROM Watched_Videos")
    suspend fun getCount(): Int

    /**
     * Deletes a specific video from the watch history.
     */
    @Query("DELETE FROM Watched_Videos WHERE video_id = :videoId")
    suspend fun deleteWatchUrl(videoId: String): Int

    /**
     * Clears the entire watch history.
     */
    @Query("DELETE FROM Watched_Videos")
    suspend fun clearAll()
}

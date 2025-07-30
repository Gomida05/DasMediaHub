package com.das.mediaHub.data.databased.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.das.mediaHub.data.databased.room.dataclass.VideoForRoom

@Dao
interface VideoDataDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(journey: VideoForRoom): Long

    @Update
    suspend fun updateVideo(videoForRoom: VideoForRoom)

    @Query("SELECT * FROM Saved_for_later ORDER BY rowid DESC")
    suspend fun getAllVideos(): List<VideoForRoom>


    @Query("SELECT * FROM Saved_for_later WHERE videoId = :videoId")
    suspend fun searchById(videoId: String): VideoForRoom?


    @Query("DELETE FROM Saved_for_later WHERE videoId = :videoId")
    suspend fun removeById(videoId: String)

}
package com.das.mediaHub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.das.mediaHub.data.model.SearchData
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchData: SearchData)

    @Query("DELETE FROM search_data WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM search_data ORDER BY id DESC")
    fun getAllSearches(): Flow<List<SearchData>>

    @Query("DELETE FROM search_data")
    suspend fun clearAll()
}
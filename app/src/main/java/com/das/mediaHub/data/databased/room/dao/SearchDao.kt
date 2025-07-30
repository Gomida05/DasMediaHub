package com.das.mediaHub.data.databased.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.das.mediaHub.data.databased.room.dataclass.SearchData

@Dao
interface SearchDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(searchData: SearchData): Long

    @Update
    suspend fun updateDate(searchData: SearchData)

    @Query("SELECT * FROM search_data ORDER BY rowid DESC")
    suspend fun getAllSearches(): List<SearchData>


    @Query("DELETE FROM search_data WHERE id = :searchId")
    suspend fun removeById(searchId: String)

}
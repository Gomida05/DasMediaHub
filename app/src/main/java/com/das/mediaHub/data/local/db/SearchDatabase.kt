package com.das.mediaHub.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.das.mediaHub.data.local.dao.SearchHistoryDao
import com.das.mediaHub.data.model.SearchData

@Database(
    entities = [SearchData::class],
    version = 2,

    exportSchema = false
)
abstract class SearchDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
}
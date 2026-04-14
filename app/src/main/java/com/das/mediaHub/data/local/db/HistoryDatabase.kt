package com.das.mediaHub.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.das.mediaHub.data.local.dao.WatchHistoryDao
import com.das.mediaHub.data.model.WatchedVideoEntity

@Database(
    entities = [WatchedVideoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
}
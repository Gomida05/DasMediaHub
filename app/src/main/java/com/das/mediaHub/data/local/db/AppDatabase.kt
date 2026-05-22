package com.das.mediaHub.data.local.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.das.mediaHub.data.local.db.dao.FavoritesDao
import com.das.mediaHub.data.local.db.dao.WatchHistoryDao
import com.das.mediaHub.data.model.SavedVideosListData
import com.das.mediaHub.data.model.WatchedVideoEntity

/**
 * Main Room database configuration for the application.
 *
 * It manages persistent storage for saved videos (favorites) and watch history.
 *
 * Example usage:
 * ```kotlin
 * val db = Room.databaseBuilder(context, AppDatabase::class.java, "media_hub.db").build()
 * ```
 */
@Database(
    entities = [
        SavedVideosListData::class,
        WatchedVideoEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    /** Returns the DAO for managing favorite videos. */
    abstract fun favoritesDao(): FavoritesDao
    
    /** Returns the DAO for managing watch history. */
    abstract fun watchHistoryDao(): WatchHistoryDao
}

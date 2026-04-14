package com.das.mediaHub.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.das.mediaHub.data.local.dao.FavoritesDao
import com.das.mediaHub.data.model.SavedVideosListData

@Database(
    entities = [SavedVideosListData::class],
    version = 4,
    exportSchema = false
)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
}
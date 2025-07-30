package com.das.mediaHub.data.databased.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.das.mediaHub.data.databased.room.dao.VideoDataDao
import com.das.mediaHub.data.databased.room.dataclass.VideoForRoom

@Database(entities = [VideoForRoom::class], version = 1)
abstract class FavoritesDatabase: RoomDatabase() {

    abstract fun videoDataDao(): VideoDataDao

    companion object {
        @Volatile private var INSTANCE: FavoritesDatabase? = null

        fun getInstance(context: Context): FavoritesDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FavoritesDatabase::class.java,
                    "favorites_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
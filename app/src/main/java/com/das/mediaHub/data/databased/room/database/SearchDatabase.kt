package com.das.mediaHub.data.databased.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.das.mediaHub.data.databased.room.dao.SearchDao
import com.das.mediaHub.data.databased.room.dataclass.SearchData

@Database(entities = [SearchData::class], version = 1)
abstract class SearchDatabase : RoomDatabase() {

    abstract fun searchDataDao(): SearchDao

    companion object {
        @Volatile private var INSTANCE: SearchDatabase? = null

        fun getInstance(context: Context): SearchDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SearchDatabase::class.java,
                    "search_history"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
package com.das.mediaHub.data.local

import android.content.Context
import androidx.room.Room
import com.das.mediaHub.data.local.db.AppMigrations
import com.das.mediaHub.data.local.db.FavoritesDatabase
import com.das.mediaHub.data.local.db.HistoryDatabase
import com.das.mediaHub.data.local.db.SearchDatabase

internal class AppDatabase (private val applicationContext: Context) {
    val searchDatabase: SearchDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            SearchDatabase::class.java,
            "search_history.db"
        )
            .addMigrations(AppMigrations.SEARCH_1_2)
            .build()
    }

    val favoritesDatabase: FavoritesDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            FavoritesDatabase::class.java,
            "favorites.db"
        )
            .addMigrations(AppMigrations.FAVORITES_3_4)
            .build()
    }

    val historyDatabase: HistoryDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            HistoryDatabase::class.java,
            "history.db"
        )
            .addMigrations(AppMigrations.HISTORY_1_2)
            .build()
    }
}
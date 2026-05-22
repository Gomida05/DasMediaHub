package com.das.mediaHub.di

import android.content.Context
import androidx.room3.Room
import com.das.mediaHub.data.local.db.AppDatabase
import com.das.mediaHub.data.local.db.dao.FavoritesDao
import com.das.mediaHub.data.local.db.dao.WatchHistoryDao
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {


    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    fun provideFavoritesDao(db: AppDatabase): FavoritesDao = db.favoritesDao()

    @Provides
    fun provideHistoryDao(db: AppDatabase): WatchHistoryDao = db.watchHistoryDao()

    @Provides
    @Singleton
    fun provideHistoryDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        runCatching {
            context.deleteDatabase("favorites.db")
            context.deleteDatabase("history.db")
            context.deleteDatabase("search_history.db")
        }
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "media_hub.db"
        )
            // If version mismatches or schema issues occur, Room wipes and recreates the DB instead of crashing
            .fallbackToDestructiveMigration()
            .build()
    }
}
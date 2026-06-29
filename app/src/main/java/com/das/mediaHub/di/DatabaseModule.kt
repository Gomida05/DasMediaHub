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

/**
 * Hilt module for providing database-related dependencies.
 * This includes Room database instances, DAOs, and Firebase Firestore.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the singleton instance of [FirebaseFirestore].
     */
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    /**
     * Provides the [FavoritesDao] from the [AppDatabase].
     */
    @Provides
    fun provideFavoritesDao(db: AppDatabase): FavoritesDao = db.favoritesDao()

    /**
     * Provides the [WatchHistoryDao] from the [AppDatabase].
     */
    @Provides
    fun provideHistoryDao(db: AppDatabase): WatchHistoryDao = db.watchHistoryDao()

    /**
     * Provides the singleton instance of [AppDatabase].
     * Performs a one-time cleanup of old database files and configures the Room database.
     *
     * @param context The application context.
     * @return The configured [AppDatabase] instance.
     */
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

package com.das.mediaHub.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.das.mediaHub.data.local.dataStore
import com.das.mediaHub.data.repository.SearchHistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing DataStore-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Provides the singleton instance of the [DataStore] for [Preferences].
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    /**
     * Provides the singleton [SearchHistoryRepository] which uses [DataStore].
     */
    @Provides
    @Singleton
    fun provideSearchHistoryRepository(dataStore: DataStore<Preferences>): SearchHistoryRepository {
        return SearchHistoryRepository(dataStore)
    }
}

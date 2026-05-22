package com.das.mediaHub.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Extension property to provide a singleton instance of [DataStore] for 
 * storing small, key-value data like search history.
 *
 * Example usage:
 * ```kotlin
 * val myDataStore = context.dataStore
 * ```
 */
internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "search_history")

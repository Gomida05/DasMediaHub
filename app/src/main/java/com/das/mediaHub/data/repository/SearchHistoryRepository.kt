package com.das.mediaHub.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.das.mediaHub.data.model.SearchData
import com.das.python.PythonMain.decodeStringToJson
import com.das.python.PythonMain.jsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository responsible for managing the user's search query history.
 *
 * It uses [DataStore] to persist the search history as a serialized JSON string.
 *
 * Example usage:
 * ```kotlin
 * @Inject lateinit var repository: SearchHistoryRepository
 * repository.insert("funny cats")
 * ```
 */
@Singleton
class SearchHistoryRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val searchHistoryKey = stringPreferencesKey("search_history_list")

    /**
     * A [Flow] that emits the current list of [SearchData] entries.
     * Decodes the serialized JSON from [DataStore] into a Kotlin list.
     */
    val getAllSearches: Flow<List<SearchData>> = dataStore.data.map { preferences ->
        val jsonString = preferences[searchHistoryKey] ?: return@map emptyList()
        runCatching {
            jsonString.decodeStringToJson<List<SearchData>>()
        }
            .getOrElse { emptyList() }
    }

    /**
     * Inserts a new search query into the history.
     * 
     * Handles duplicates by moving the existing entry to the top and limits the total 
     * history to 15 items.
     *
     * @param value The search string to insert.
     */
    suspend fun insert(value: String) {
        dataStore.edit { preferences ->
            val currentList = getCurrentList(preferences)

            // Remove duplicate if the user searches the same thing again
            val filteredList = currentList.filterNot { it.value.equals(value, ignoreCase = true) }

            val newItem = SearchData(id = System.currentTimeMillis().toString(), value = value)

            // Put newest search at the top, limit to 15 items total
            val updatedList = (listOf(newItem) + filteredList).take(15)

            preferences[searchHistoryKey] = Json.encodeToString(updatedList)
        }
    }

    /**
     * Deletes a specific search history entry by its unique ID.
     * @param id The identifier of the search entry to remove.
     */
    suspend fun deleteById(id: String) {
        dataStore.edit { preferences ->
            val currentList = getCurrentList(preferences)
            val updatedList = currentList.filterNot { it.id == id }
            preferences[searchHistoryKey] = jsonParser.encodeToString(updatedList)
        }
    }

    /**
     * Clears the entire search history from [DataStore].
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(searchHistoryKey)
        }
    }

    /**
     * Helper method to retrieve the current search list from preferences.
     */
    private fun getCurrentList(preferences: Preferences): List<SearchData> {
        val jsonString = preferences[searchHistoryKey] ?: return emptyList()
        return runCatching { Json.decodeFromString<List<SearchData>>(jsonString) }.getOrElse { emptyList() }
    }
}

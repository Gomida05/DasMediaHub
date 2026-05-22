package com.das.downloader.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.das.downloader.data.model.download.DownloadState
import com.das.downloader.data.model.download.DownloadStatus
import com.das.downloader.data.model.download.DownloadType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistence layer for saving and loading download states using [SharedPreferences].
 * 
 * It converts a list of [DownloadState] objects into a JSON array for storage.
 * Operations are thread-safe via a [Mutex].
 * 
 * Example usage:
 * ```kotlin
 * val store = DownloadStateStore(prefs)
 * store.saveAll(listOf(downloadState))
 * val savedStates = store.loadAll()
 * ```
 */
class DownloadStateStore(
    private val prefs: SharedPreferences
) {

    private val mutex = Mutex()

    /**
     * Persists all provided download states to disk.
     * @param states The list of [DownloadState] objects to save.
     */
    suspend fun saveAll(states: List<DownloadState>) {
        mutex.withLock {
            val json = JSONArray()
            states.forEach { state ->
                json.put(
                    JSONObject().apply {
                        put("id", state.id)
                        put("url", state.url)
                        put("title", state.title)
                        put("type", state.type.name)
                        put("destinationPath", state.destinationPath)
                        put("status", state.status.name)
                        put("progress", state.progress)
                        put("downloadedBytes", state.downloadedBytes)
                        put("totalBytes", state.totalBytes)
                        put("errorMessage", state.errorMessage)
                        put("playlistName", state.playlistName)
                    }
                )
            }
            prefs.edit { putString("states", json.toString()) }
        }
    }

    /**
     * Loads all persisted download states from disk.
     * @return A list of [DownloadState] objects, or an empty list if none are saved.
     */
    suspend fun loadAll(): List<DownloadState> {
        return mutex.withLock {
            val raw = prefs.getString("states", null) ?: return@withLock emptyList()
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        DownloadState(
                            id = obj.getString("id"),
                            url = obj.getString("url"),
                            title = obj.getString("title"),
                            type = DownloadType.valueOf(obj.getString("type")),
                            destinationPath = obj.getString("destinationPath"),
                            status = DownloadStatus.valueOf(obj.getString("status")),
                            progress = obj.getInt("progress"),
                            downloadedBytes = obj.getLong("downloadedBytes"),
                            totalBytes = obj.getLong("totalBytes"),
                            errorMessage = obj.optString("errorMessage", ""),
                            playlistName = obj.optString("playlistName", "")
                        )
                    )
                }
            }
        }
    }
}

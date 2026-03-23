package com.das.mediaHub.data.local

import android.content.Context
import androidx.core.content.edit
import com.das.mediaHub.data.model.download.DownloadState
import com.das.mediaHub.data.model.download.DownloadStatus
import com.das.mediaHub.data.model.download.DownloadType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

class DownloadStateStore(context: Context) {

    private val prefs = context.getSharedPreferences("download_state_store", Context.MODE_PRIVATE)
    private val mutex = Mutex()

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
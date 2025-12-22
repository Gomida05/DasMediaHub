package com.das.mediaHub.ui.settings.watch_later

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.local.DatabaseFavorite
import com.das.mediaHub.data.model.SavedVideosListData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchLaterViewModel(application: Application) : AndroidViewModel(application) {

    val dbHelper = DatabaseFavorite(getApplication())

    private val _searchResults = MutableStateFlow<List<SavedVideosListData>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    fun fetchData() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {

                _searchResults.value = fetchDataFromDatabase()

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }



    private suspend fun fetchDataFromDatabase(): List<SavedVideosListData> =
        withContext(Dispatchers.IO) {

            val cursor = dbHelper.getResults()
            val savedVideosListData = mutableListOf<SavedVideosListData>()

            try {
                while (cursor.moveToNext()) {
                    val watchUrl = cursor.getString(cursor.getColumnIndexOrThrow("video_id"))
                    val title = dbHelper.getVideoTitle(watchUrl).toString()
                    val viewerNumber = cursor.getString(cursor.getColumnIndexOrThrow("viewNumber"))
                    val dateTime = cursor.getString(cursor.getColumnIndexOrThrow("videoDate"))
                    val channelName =
                        cursor.getString(cursor.getColumnIndexOrThrow("videoChannelName"))
                    val myDuration = cursor.getString(cursor.getColumnIndexOrThrow("duration"))
                    val channelThumbnail =
                        cursor.getString(cursor.getColumnIndexOrThrow("channelThumbnail"))

                    savedVideosListData.add(
                        SavedVideosListData(
                            title,
                            watchUrl,
                            "https://img.youtube.com/vi/$watchUrl/0.jpg",
                            viewerNumber,
                            dateTime,
                            myDuration,
                            channelName,
                            channelThumbnail
                        )
                    )
                }
            } finally {
                cursor.close()
            }

            savedVideosListData
        }

    fun removeSearchItem(searchItem: SavedVideosListData) {
        _searchResults.value = _searchResults.value.filter { it != searchItem }
    }

}
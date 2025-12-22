package com.das.mediaHub.ui.watchedVideos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.local.WatchHistory
import com.das.mediaHub.data.model.SavedVideosListData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchedVideosViewModel(application: Application): AndroidViewModel(application) {

    val dbHelper = WatchHistory(application)

    private val _savedLists = MutableStateFlow<List<SavedVideosListData>>(emptyList())
    val savedLists = _savedLists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val isError = _error.asStateFlow()

    fun fetchData() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = fetchDataFromDatabase()
                _savedLists.value = result
            } catch (e: Exception) {
                _error.value = "Something went wrong: ${e.message}"
            }
            finally {
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
                val viewerNumber = dbHelper.getViewNumber(watchUrl).toString()
                val dateTime = dbHelper.getVideoDate(watchUrl).toString()
                val channelName = dbHelper.getVideoChannelName(watchUrl).toString()
                val myDuration = dbHelper.getDuration(watchUrl).toString()
                val channelThumbnail = dbHelper.getChannelNameThumbnail(watchUrl).toString()
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

            savedVideosListData
        } finally {
            cursor.close()
        }
    }


    fun removeSearchItem(searchItem: SavedVideosListData) {
        _savedLists.value = _savedLists.value.filter { it != searchItem }
    }
}
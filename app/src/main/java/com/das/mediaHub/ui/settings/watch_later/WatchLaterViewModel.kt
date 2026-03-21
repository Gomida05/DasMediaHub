package com.das.mediaHub.ui.settings.watch_later

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.local.DatabaseFavorite
import com.das.mediaHub.data.model.SavedVideosListData
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchLaterViewModel(private val dbHelper: DatabaseFavorite) : ViewModel() {

    private val _searchResults = MutableStateFlow<UiState<List<SavedVideosListData>>>(UiState.Idle)
    val searchResults = _searchResults.asStateFlow()

    fun fetchData() {
        _searchResults.value = UiState.Loading
        viewModelScope.launch {
            try {

                val savedVideosListData = fetchDataFromDatabase()
                if (savedVideosListData.isEmpty()) {
                    _searchResults.value = UiState.Empty
                } else {
                    _searchResults.value = UiState.Success(savedVideosListData)
                }

            } catch (e: Exception) {
                _searchResults.value = UiState.Error(message = e.message ?: "Unknown error")
            }
        }
    }



    private suspend fun fetchDataFromDatabase(): List<SavedVideosListData> =
        withContext(Dispatchers.IO) {

            val cursor = dbHelper.getResults()
            val savedVideosListData = mutableListOf<SavedVideosListData>()

            cursor.use { cursor ->
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
            }

            savedVideosListData
        }

    fun removeSearchItem(searchItem: SavedVideosListData) {
        val currentState = _searchResults.value

        if (currentState is UiState.Success) {
            val updatedList = currentState.data.filter { it != searchItem }

            _searchResults.value = if (updatedList.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(updatedList)
            }
        }
    }

}
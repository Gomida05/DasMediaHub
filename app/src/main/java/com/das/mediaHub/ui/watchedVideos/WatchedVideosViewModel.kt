package com.das.mediaHub.ui.watchedVideos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.local.WatchHistory
import com.das.mediaHub.data.model.SavedVideosListData
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchedVideosViewModel(private val dbHelper: WatchHistory): ViewModel() {

    private val _savedListsState = MutableStateFlow<UiState<List<SavedVideosListData>>>(UiState.Idle)
    val savedListState = _savedListsState.asStateFlow()


    fun fetchData() {
        _savedListsState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val result = fetchDataFromDatabase()

                _savedListsState.value = if (result.isEmpty()) UiState.Empty else UiState.Success(result)
            } catch (e: Exception) {
                _savedListsState.value = UiState.Error(e.message ?: "Unknown error")
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
            }
    }


    fun removeSearchItem(searchItem: SavedVideosListData) {
        val currentState = _savedListsState.value

        if (currentState is UiState.Success) {
            val updatedList = currentState.data.filter { it != searchItem }

            _savedListsState.value = if (updatedList.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(updatedList)
            }
        }
    }
}
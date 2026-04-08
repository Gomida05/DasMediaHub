package com.das.mediaHub.ui.watchedVideos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.local.WatchHistory
import com.das.mediaHub.data.model.SavedVideosListData
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchedVideosViewModel(
    private val dbHelper: WatchHistory
) : ViewModel() {

    private val _savedListsState = MutableStateFlow<UiState<List<SavedVideosListData>>>(UiState.Idle)
    val savedListState = _savedListsState.asStateFlow()

    fun fetchData() {
        _savedListsState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    dbHelper.getWatchedVideos()
                }

                _savedListsState.value =
                    if (result.isEmpty()) UiState.Empty else UiState.Success(result)
            } catch (e: Exception) {
                _savedListsState.value = UiState.Error(ErrorMapper.map(e))
            }
        }
    }

    fun deleteVideo(videoId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.deleteWatchUrl(videoId)
            }
            removeSearchItemFromState(videoId = videoId)
        }
    }

    private fun removeSearchItemFromState(videoId: String) {
        val currentState = _savedListsState.value

        if (currentState is UiState.Success) {
            val updatedList = currentState.data.filter { it.watchUrl != videoId }
            _savedListsState.value =
                if (updatedList.isEmpty()) UiState.Empty else UiState.Success(updatedList)
        }
    }
}
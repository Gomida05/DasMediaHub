package com.das.mediaHub.ui.watch_later

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.VideoItem
import com.das.mediaHub.data.model.VideoItem.Companion.toVideoItem
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.data.repository.FavoritesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedVideosViewModel(private val dbHelper: FavoritesRepository) : ViewModel() {

    private val _searchResults = MutableStateFlow<UiState<List<VideoItem>>>(UiState.Idle)
    val searchResults = _searchResults.asStateFlow()

    fun fetchData() {
        _searchResults.value = UiState.Loading
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    dbHelper.getAllSavedVideos()
                }
                _searchResults.value = if (result.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(result.map { it.toVideoItem() })
                }

            } catch (e: Exception) {
                _searchResults.value = UiState.Error(message = ErrorMapper.map(e))
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
        val currentState = _searchResults.value
        if (currentState is UiState.Success) {
            val updatedList = currentState.data.filter { it.watchUrl != videoId }

            _searchResults.value = if (updatedList.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(updatedList)
            }
        }
    }

}
package com.das.mediaHub.ui.result

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.interfaces.UiState
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.toVideosListData
import com.das.python.YouTuber
import com.das.python.YouTuber.loadStreamUrl
import com.das.python.data.model.ItemsStreamUrlsForMediaItemData
import com.das.python.data.model.searcher.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel: ViewModel() {


    private val _searchResults = MutableStateFlow<UiState<List<Video>>>(UiState.Idle)
    val searchResults = _searchResults.asStateFlow()


    private val _allResults = mutableListOf<Video>()
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()




    private var currentBatch = 0
    private val batchSize = 20

    private var currentQuery: String? = null
    private var nextPageToken: String? = null

    fun loadInitialIfNeeded(query: String) {
        val state = _searchResults.value

        if (currentQuery == query && state is UiState.Success && state.data.isNotEmpty()) return
        if (currentQuery == query && state is UiState.Loading) return

        currentQuery = query
        fetchSuggestions(query)
    }

    fun retry(query: String) {
        currentQuery = query
        fetchSuggestions(query)
    }

    fun fetchSuggestions(query: String) {
        currentQuery = query
        nextPageToken = null
        _searchResults.value = UiState.Loading

        viewModelScope.launch {
            try {
                val result = YouTuber.search(query)
                val newResult = result.result
                val resultValue = newResult?.result

                if (result.success && resultValue != null) {
                    _allResults.clear()
                    _allResults.addAll(
                        elements = resultValue
                            .asSequence()
                            .distinctBy { it.id }
                            .toList()
                    )
                    currentBatch = 1

                    val firstBatch = _allResults.take(batchSize)

                    _searchResults.value = when {
                        firstBatch.isEmpty() -> UiState.Empty
                        else -> UiState.Success(firstBatch)
                    }
                } else {
                    _searchResults.value = UiState.Error(
                        ErrorMapper.mapMessage(result.error)
                    )
                }
            } catch (e: Exception) {
                Log.d(
                    "Error is here",
                    "error message ${e.message}",
                    e
                )
                _searchResults.value = UiState.Error(ErrorMapper.map(e))
            }
        }
    }

    fun loadMore() {
        val query = currentQuery ?: return
        val currentState = _searchResults.value as? UiState.Success ?: return
        if (nextPageToken.isNullOrBlank()) return
        if (_isLoadingMore.value || _allResults.isEmpty()) return

        _isLoadingMore.value = true

        viewModelScope.launch {
            val nextBatchStart = currentBatch * batchSize
            val moreItems = _allResults.drop(nextBatchStart).take(batchSize)

            if (moreItems.isNotEmpty()) {
                _searchResults.value = UiState.Success(currentState.data + moreItems)
                currentBatch++
            }

            _isLoadingMore.value = false
        }
    }

    fun loadStreamUrl(
        mediaItem: Video,
        onStart: () -> Unit,
        onSuccess: (ItemsStreamUrlsForMediaItemData) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        onStart()
        viewModelScope.launch {
            mediaItem.toVideosListData().loadStreamUrl(
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        }
    }
}
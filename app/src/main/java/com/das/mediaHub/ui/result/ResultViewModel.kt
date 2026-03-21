package com.das.mediaHub.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import com.das.python.YouTuber
import com.das.python.data.model.searcher.Video
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel: ViewModel() {


    private val _searchResults = MutableStateFlow<UiState<List<Video>>>(UiState.Idle)
    val searchResults = _searchResults.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _allResults = mutableListOf<Video>()
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()




    private var currentBatch = 0
    private val batchSize = 20



    fun fetchSuggestions(inputText: String) {
        _searchResults.value = UiState.Loading

        viewModelScope.launch {
            try {
                val result = YouTuber.search(inputText)
                val newResult = result.result?.result.orEmpty()

                if (result.success) {
                    _allResults.clear()
                    _allResults.addAll(
                        elements = newResult
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
                        result.error ?: "Something went wrong!"
                    )
                }
            } catch (e: Exception) {
                _searchResults.value = UiState.Error(
                    e.message ?: "Something went wrong!"
                )
            }
        }
    }

    fun loadMore() {
        val currentState = _searchResults.value
        if (_isLoadingMore.value || _allResults.isEmpty() || currentState !is UiState.Success) return

        _isLoadingMore.value = true

        viewModelScope.launch {
            delay(500)

            val nextBatchStart = currentBatch * batchSize
            val moreItems = _allResults.drop(nextBatchStart).take(batchSize)

            if (moreItems.isNotEmpty()) {
                _searchResults.value = UiState.Success(currentState.data + moreItems)
                currentBatch++
            }

            _isLoadingMore.value = false
        }
    }

}
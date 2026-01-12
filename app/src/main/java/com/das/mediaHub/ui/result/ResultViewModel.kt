package com.das.mediaHub.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.model.responds.ResponseVideo
import com.das.mediaHub.data.model.searcher.Video
import com.das.mediaHub.python.PythonMain.callMethod
import com.das.mediaHub.python.PythonMain.pythonInstant
import com.das.mediaHub.python.data.Names.SEARCHER
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel: ViewModel() {


    private val _searchResults = MutableStateFlow<List<Video>>(emptyList())
    val searchResults = _searchResults.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _allResults = mutableListOf<Video>()
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)

    val error = _error.asStateFlow()


    private var currentBatch = 0
    private val batchSize = 20


    private var hasLoaded = false

    fun fetchSuggestionsIfNeeded(inputText: String) {
        if (hasLoaded) return
        hasLoaded = true
        fetchSuggestions(inputText)
    }

    fun fetchSuggestions(inputText: String) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = callPythonForSearchVideos(inputText)
                if (result.success && result.result != null) {
                    _allResults.clear()
                    _allResults.addAll(result.result.result)

                    currentBatch = 1
                    _searchResults.value = _allResults.take(batchSize)
                }
                else {
                    _error.value = result.error ?: "Something went wrong!"
                }

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {

        if (_isLoadingMore.value || _allResults.isEmpty()) return

        _isLoadingMore.value = true
        viewModelScope.launch {
            delay(500)
            val nextBatch = currentBatch * batchSize
            val moreItems = _allResults.drop(nextBatch).take(batchSize)

            if (moreItems.isNotEmpty()) {
                _searchResults.value += moreItems
                currentBatch++
            }
            _isLoadingMore.value = false
        }

    }

    private suspend fun callPythonForSearchVideos(inputText: String): ResponseVideo {
        return pythonInstant.callMethod<ResponseVideo>(name = SEARCHER, args = inputText)
    }


}
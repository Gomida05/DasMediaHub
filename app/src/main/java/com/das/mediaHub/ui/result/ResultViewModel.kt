package com.das.mediaHub.ui.result

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.python.YouTuber.pythonInstant
import com.das.mediaHub.data.model.responds.ResponseVideo
import com.das.mediaHub.data.model.searcher.Video
import com.das.mediaHub.python.Main.callMethod
import com.das.mediaHub.python.data.Names.SEARCHER
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ResultViewModel: ViewModel() {


    private val _searchResults = mutableStateOf<List<Video>>(emptyList())
    val searchResults: State<List<Video>> = _searchResults
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _allResults = mutableListOf<Video>()
    private val _isLoadingMore = mutableStateOf(false)
    val isLoadingMore: State<Boolean> = _isLoadingMore

    private val _error = mutableStateOf<String?>(null)

    val error: State<String?> = _error


    private var currentBatch = 0
    private val batchSize = 20

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
                _searchResults.value = _searchResults.value + moreItems
                currentBatch++
            }
            _isLoadingMore.value = false
        }

    }

    private suspend fun callPythonForSearchVideos(inputText: String): ResponseVideo {
        return try {
            pythonInstant.callMethod(name = SEARCHER, args = inputText)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


}
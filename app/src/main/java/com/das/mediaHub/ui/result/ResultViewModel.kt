package com.das.mediaHub.ui.result

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.YouTuber.pythonInstant
import com.das.mediaHub.data.model.searcher.SearchResponse
import com.das.mediaHub.data.model.searcher.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ResultViewModel: ViewModel() {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val _searchResults = mutableStateOf<List<Video>>(emptyList())
    val searchResults: State<List<Video>> = _searchResults
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)

    val error = _error


    fun fetchSuggestions(inputText: String) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = callPythonForSearchVideos(inputText)
                _searchResults.value = result

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun callPythonForSearchVideos(inputText: String): List<Video> {
        return try {
            val python = pythonInstant.getModule("main")

            val variable = withContext(Dispatchers.IO){
                python["Searcher"]?.call(inputText)
            }

            if (variable.isNullOrEmpty() || variable.toString() == "False"){
                throw Exception(variable.toString())
            }else {
                val videoList = jsonParser.decodeFromString<SearchResponse>(variable.toString())
                videoList.result
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


}
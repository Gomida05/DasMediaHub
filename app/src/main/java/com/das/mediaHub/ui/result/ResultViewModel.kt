package com.das.mediaHub.ui.result

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.YouTuber.pythonInstant
import com.das.mediaHub.data.model.SearchResultFromMain
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultViewModel: ViewModel() {

    private val _searchResults = mutableStateOf<List<SearchResultFromMain>>(emptyList())
    val searchResults: State<List<SearchResultFromMain>> = _searchResults
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isThereError = mutableStateOf<String?>(null)

    val error = _isThereError


    fun fetchSuggestions(inputText: String) {
        _isLoading.value = true
        _isThereError.value = null

        viewModelScope.launch {
            try {
                val result = callPythonForSearchVideos(inputText)
                _searchResults.value = result

            } catch (e: Exception) {
                _isThereError.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun callPythonForSearchVideos(inputText: String): List<SearchResultFromMain> {
        return try {
            val python = pythonInstant.getModule("main")

            val variable = withContext(Dispatchers.IO){
                python["Searcher"]?.call(inputText)
            }

            if (variable.isNullOrEmpty() || variable.toString() == "False"){
                throw Exception(variable.toString())
            }else {
                val videoListType = object : TypeToken<List<SearchResultFromMain>>() {}.type
                val videoList: List<SearchResultFromMain> = Gson().fromJson(variable.toString(), videoListType)
                videoList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


}
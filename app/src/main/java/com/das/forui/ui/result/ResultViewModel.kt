package com.das.forui.ui.result

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.forui.MainApplication.Youtuber.pythonInstant
import com.das.forui.objectsAndData.ForUIDataClass.SearchResultFromMain
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultViewModel: ViewModel() {

    private val _searchResults = mutableStateOf<List<SearchResultFromMain>>(emptyList())
    val searchResults: State<List<SearchResultFromMain>> = _searchResults
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading


    fun fetchSuggestions(inputText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = callPythonForSearchVideos(inputText)
            withContext(Dispatchers.Main) {
                _searchResults.value = result ?: emptyList()
                _isLoading.value = false
            }
        }
    }


    private fun callPythonForSearchVideos(inputText: String): List<SearchResultFromMain>? {
        return try {
            val mainFile = pythonInstant.getModule("main")
            val variable = mainFile["Searcher"]?.call(inputText).toString()
            val videoListType = object : TypeToken<List<SearchResultFromMain>>() {}.type
            val videoList: List<SearchResultFromMain> = Gson().fromJson(variable, videoListType)
            videoList
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
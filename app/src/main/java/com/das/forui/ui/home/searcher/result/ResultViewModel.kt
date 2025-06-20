package com.das.forui.ui.home.searcher.result

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.forui.objectsAndData.Youtuber.pythonInstant
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

    private val _isThereError = mutableStateOf<String?>(null)

    val error = _isThereError


    fun fetchSuggestions(inputText: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO){
                callPythonForSearchVideos(inputText)
            }
            _searchResults.value = result ?: emptyList()
            _isLoading.value = false
        }
    }


    private fun callPythonForSearchVideos(inputText: String): List<SearchResultFromMain>? {
        return try {
            val variable = pythonInstant["Searcher"]?.call(inputText)
            if (variable.isNullOrEmpty() || variable.toString() == "False"){
                _isThereError.value = variable.toString()
                null
            }else {
                val videoListType = object : TypeToken<List<SearchResultFromMain>>() {}.type
                val videoList: List<SearchResultFromMain> = Gson().fromJson(variable.toString(), videoListType)
                videoList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isThereError.value = e.message
            return null
        }
    }
}
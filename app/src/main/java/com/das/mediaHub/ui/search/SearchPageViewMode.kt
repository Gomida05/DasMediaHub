package com.das.mediaHub.ui.search

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.local.SearchHistoryDB
import com.das.mediaHub.data.model.SearchData
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchPageViewMode(private val db: SearchHistoryDB): ViewModel() {

    private val _searchHistory = MutableStateFlow<UiState<List<SearchData>>>(UiState.Idle)
    val searchHistory = _searchHistory.asStateFlow()


    var query = mutableStateOf(TextFieldValue(""))
        private set

    fun setQuery(value: TextFieldValue) {
        query.value = value
    }

    fun addNew(searchKey: String) {

        viewModelScope.launch {
            try {
                val id = System.currentTimeMillis().toString()
                val searchData = SearchData(id = id, value = searchKey)
                db.insert(searchData)
            } catch (e: Exception) {
                println("Something went wrong: ${e.message}")
                _searchHistory.value = UiState.Error(message = ErrorMapper.map(e))
            }
        }
    }

    fun fetchDatabase() {

        _searchHistory.value = UiState.Loading

        viewModelScope.launch {
            try {
                val result = db.getAllSearches()
                if (result.isEmpty()) _searchHistory.value = UiState.Empty
                else _searchHistory.value = UiState.Success(result)
            } catch (e: Exception) {
                _searchHistory.value = UiState.Error(message = ErrorMapper.map(e))
            }
        }
    }

    fun deleById(id: String) {

        viewModelScope.launch {
            db.delete(id)
            fetchDatabase()
        }

    }

}
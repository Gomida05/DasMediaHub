package com.das.mediaHub.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.model.SearchData
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchPageViewModel(
    private val repository: SearchRepository
) : ViewModel() {

    private val _searchHistory = MutableStateFlow<UiState<List<SearchData>>>(UiState.Idle)
    val searchHistory = _searchHistory.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    private var currentQuery = ""


    init {
        observeSearchHistory()
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun seedQueryIfEmpty(value: String) {
        if (currentQuery.isNotBlank() && value.isBlank() && currentQuery != _query.value) {
            _query.value = currentQuery
            return
        }

        if (value.isNotBlank()) {
            currentQuery = value
            _query.value = value
        }
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun addNew(searchKey: String) {
        if (searchKey.isBlank()) return

        viewModelScope.launch {
            try {
                repository.insert(searchKey.trim())
            } catch (e: Exception) {
                _searchHistory.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteById(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteById(id)
            } catch (e: Exception) {
                _searchHistory.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun observeSearchHistory() {
        viewModelScope.launch {
            try {
                repository.getAllSearches().collect { result ->
                    _searchHistory.value =
                        if (result.isEmpty()) UiState.Empty else UiState.Success(result)
                }
            } catch (e: Exception) {
                _searchHistory.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
package com.das.mediaHub.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.SearchData
import com.das.mediaHub.data.model.interfaces.UiState
import com.das.mediaHub.data.repository.SearchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchHistoryRepository
) : ViewModel() {

    val searchHistory: StateFlow<UiState<List<SearchData>>> = repository.getAllSearches
        .map { result ->
            if (result.isEmpty()) UiState.Empty else UiState.Success(result)
        }
        .onStart {
            emit(UiState.Loading)
        }
        .catch { e ->
            emit(UiState.Error(ErrorMapper.map(e)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keeps upstream alive for 5s after UI rotates or goes background
            initialValue = UiState.Idle
        )

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    var query = MutableStateFlow("")
        private set

    private var currentQuery = ""

    fun setQuery(value: String) {
        query.value = value
    }

    fun seedQueryIfEmpty(value: String) {
        if (currentQuery.isNotBlank() && value.isBlank() && currentQuery != query.value) {
            query.value = currentQuery
            return
        }

        if (value.isNotBlank()) {
            currentQuery = value
            query.value = value
        }
    }

    fun addNew(searchKey: String) {
        if (searchKey.isBlank()) return

        viewModelScope.launch {
            runCatching {
                repository.insert(searchKey.trim())
            }.onFailure { e ->
                reportFailure(e)
            }
        }
    }

    fun deleteById(id: String) {
        viewModelScope.launch {
            runCatching {
                repository.deleteById(id)
            }.onFailure { e ->
                reportFailure(e)
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            runCatching {
                repository.clearAll()
            }.onFailure { e ->
                reportFailure(e)
            }
        }
    }

    private fun reportFailure(e: Throwable) {
        viewModelScope.launch {
            _errorFlow.emit(ErrorMapper.map(e))
        }
    }
}
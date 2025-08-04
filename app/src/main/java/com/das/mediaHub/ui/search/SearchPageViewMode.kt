package com.das.mediaHub.ui.search

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.databased.room.database.SearchDatabase
import com.das.mediaHub.data.databased.room.dataclass.SearchData
import com.das.mediaHub.data.databased.room.repository.SearchRepo
import kotlinx.coroutines.launch

class SearchPageViewMode(application: Application): AndroidViewModel(application) {


    private val db = SearchDatabase.getInstance(application)
    private val repository = SearchRepo(db.searchDataDao())

    private val _loading = mutableStateOf(false)
    val isLoading: State<Boolean> = _loading

    private val _searchHistory = mutableStateOf<List<SearchData>>(emptyList())
    val searchHistory: State<List<SearchData>> = _searchHistory

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error


    fun addNew(searchKey: String) {
        viewModelScope.launch {
            try {
                val id = System.currentTimeMillis().toString()
                val searchData = SearchData(id = id, value = searchKey)
                repository.insert(searchData)
            } catch (e: Exception) {
                println("Something went wrong: ${e.message}")
            }
        }
    }

    fun fetchDatabase() {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = repository.getAllSearches()
                _searchHistory.value = result
            } catch (e: Exception) {
                _error.value = "Something went wrong: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleById(id: String) {

        viewModelScope.launch {
            repository.delete(id)
            fetchDatabase()
        }

    }

}
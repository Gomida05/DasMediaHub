package com.das.forui.ui.home.searcher

import android.app.Application
import android.database.Cursor
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.das.forui.databased.SearchHistoryDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchPageViewMode(private val application: Application): AndroidViewModel(application) {
    private val _searchHistory = mutableStateOf<List<String>>(emptyList())
    val downloadedListData: MutableState<List<String>> = _searchHistory

    private val _error = mutableStateOf("")
    val error: State<String> = _error

    fun fetchDatabase() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                fetchDataFromDatabase(application)
            }
            _searchHistory.value = result
        }
    }

    private fun fetchDataFromDatabase(application: Application): List<String> {
        try {
            val dbHelper = SearchHistoryDB(application)

            val cursor: Cursor? = dbHelper.getResults()
            val urls = mutableListOf<String>()
            cursor?.let {
                while (it.moveToNext()) {
                    val title = it.getString(it.getColumnIndexOrThrow("title"))
                    title?.let { _ ->
                        urls.add("$title ")
                    } ?: run {}
                }
                it.close()
            } ?: run {}

            return urls.toList()
        } catch (e: Exception) {
            _error.value = e.message.toString()
        }
        return listOf("")

    }
}
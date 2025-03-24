package com.das.forui.ui.watch_later

import android.app.Application
import android.database.Cursor
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.das.forui.databased.DatabaseFavorite
import com.das.forui.objectsAndData.SavedVideosListData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchLaterViewModel(application: Application) : AndroidViewModel(application) {

    private val _searchResults = mutableStateOf<List<SavedVideosListData>>(emptyList())
    val searchResults: State<List<SavedVideosListData>> = _searchResults

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    fun fetchData() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                fetchDataFromDatabase()
            }
            withContext(Dispatchers.Main) {
                _searchResults.value = result ?: emptyList()
                _isLoading.value = false
            }
        }
    }

    private fun fetchDataFromDatabase(): MutableList<SavedVideosListData>? {
        val globalContext = getApplication<Application>().applicationContext
        val dbHelper = DatabaseFavorite(globalContext)
        val cursor: Cursor? = dbHelper.getResults()

        val savedVideosListData = mutableListOf<SavedVideosListData>()
        try {
            cursor?.let {
                while (it.moveToNext()) {
                    val watchUrl = it.getString(it.getColumnIndexOrThrow("video_id"))
                    val title = dbHelper.getVideoTitle(watchUrl).toString()
                    val viewerNumber = it.getString(it.getColumnIndexOrThrow("viewNumber"))
                    val dateTime = it.getString(it.getColumnIndexOrThrow("videoDate"))
                    val channelName = it.getString(it.getColumnIndexOrThrow("videoChannelName"))
                    val myDuration = it.getString(it.getColumnIndexOrThrow("duration"))
                    val channelThumbnail = it.getString(it.getColumnIndexOrThrow("channelThumbnail"))
                    savedVideosListData.add(
                        SavedVideosListData(
                            title,
                            watchUrl,
                            "https://img.youtube.com/vi/$watchUrl/0.jpg",
                            viewerNumber,
                            dateTime,
                            myDuration,
                            channelName,
                            channelThumbnail
                        )
                    )
                }
                it.close()
            }

            return savedVideosListData
        } catch (e: Exception) {
            Toast.makeText(globalContext, "${e.message}", Toast.LENGTH_SHORT).show()
            return null
        }
    }


    fun removeSearchItem(searchItem: SavedVideosListData) {
        _searchResults.value = _searchResults.value.filter { it != searchItem }
    }

}
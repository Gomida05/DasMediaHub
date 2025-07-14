package com.das.forui.ui.watchedVideos

import android.app.Application
import android.database.Cursor
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.das.forui.data.databased.WatchHistory
import com.das.forui.data.model.SavedVideosListData
import com.das.forui.data.Youtuber.getListItemStreamUrl
import com.das.forui.data.model.ItemsStreamUrlsForMediaItemData
import com.das.forui.data.model.VideosListData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchedVideosViewModel(application: Application): AndroidViewModel(application) {
    private val _savedLists = mutableStateOf<List<SavedVideosListData>>(emptyList())
    val savedLists: State<List<SavedVideosListData>> = _savedLists

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    fun fetchData() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                fetchDataFromDatabase()
            }
            _savedLists.value = result ?: emptyList()
            _isLoading.value = false

        }
    }

    fun getListItemsStreamUrls(
        data: VideosListData,
        onSuccess: (ItemsStreamUrlsForMediaItemData) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    getListItemStreamUrl(data)
                }

                onSuccess(result)
            } catch (e: Exception) {
                onFailure("Failed: ${e.message}")
            }
        }
    }

    private fun fetchDataFromDatabase(): MutableList<SavedVideosListData>? {
        val globalContext = getApplication<Application>().applicationContext
        val dbHelper = WatchHistory(globalContext)
        val cursor: Cursor? = dbHelper.getResults()

        val savedVideosListData = mutableListOf<SavedVideosListData>()
        try {
            cursor?.let {
                while (it.moveToNext()) {
                    val watchUrl = it.getString(it.getColumnIndexOrThrow("video_id"))
                    val title = dbHelper.getVideoTitle(watchUrl).toString()
                    val viewerNumber = dbHelper.getViewNumber(watchUrl).toString()
                    val dateTime = dbHelper.getVideoDate(watchUrl).toString()
                    val channelName = dbHelper.getVideoChannelName(watchUrl).toString()
                    val myDuration = dbHelper.getDuration(watchUrl).toString()
                    val channelThumbnail = dbHelper.getChannelNameThumbnail(watchUrl).toString()
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
        _savedLists.value = _savedLists.value.filter { it != searchItem }
    }
}
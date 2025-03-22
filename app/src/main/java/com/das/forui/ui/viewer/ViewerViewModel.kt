package com.das.forui.ui.viewer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.forui.MainApplication.Youtuber.formatDate
import com.das.forui.MainApplication.Youtuber.formatViews
import com.das.forui.MainApplication.Youtuber.pythonInstant
import com.das.forui.objectsAndData.VideoDetails
import com.das.forui.objectsAndData.VideosListData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ViewerViewModel : ViewModel() {
    private val _videoUrl = MutableLiveData<String>()
    val videoUrl: LiveData<String> = _videoUrl

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _videoDetails = MutableLiveData<VideoDetails>()
    val videoDetails: LiveData<VideoDetails> = _videoDetails

    private val _searchResults = mutableStateOf<List<VideosListData>>(emptyList())
    val searchResults: State<List<VideosListData>> = _searchResults
    private val _isLoadingVideos = mutableStateOf(true)
    val isLoadingVideos: State<Boolean> = _isLoadingVideos

    fun loadVideoUrl(videoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {

                val mainFile = pythonInstant.getModule("main")
                val variable = mainFile["get_video_url"]
                val result = variable?.call("https://www.youtube.com/watch?v=$videoId").toString()

                if (result != "False") {
                    _videoUrl.postValue(result)
                } else {
                    _error.postValue("Something went wrong $result")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    fun fetchVideoDetails(videoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                val videoDetails = callPythonSearchWithLink(videoId)

                if (videoDetails != null) {
                    val details = VideoDetails(
                        title = videoDetails["title"].toString(),
                        viewNumber = formatViews(videoDetails["viewNumber"].toString().toLong()),
                        date = formatDate(videoDetails["date"].toString()),
                        channelName = videoDetails["channelName"].toString(),
                        description = videoDetails["description"].toString()
                    )
                    withContext(Dispatchers.Main) {
                        _videoDetails.postValue(details)
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        _error.postValue("Failed to fetch video details")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.postValue("Error fetching video details: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.postValue(false)
                }
            }
        }
    }


    fun fetchSuggestions(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingVideos.value = true
            val result = callPythonSearchSuggestion(title)
            withContext(Dispatchers.Main) {
                _searchResults.value = result ?: emptyList()
                _isLoadingVideos.value = false
            }
        }
    }

    private fun callPythonSearchWithLink(inputText: String): Map<String, Any>? {
        return try {
            val mainFile = pythonInstant.getModule("main")
            val variable = mainFile["SearchWithLink"]
            val result = variable?.call("https://www.youtube.com/watch?v=$inputText")
            println("python: $result")

            // Use Gson to parse the JSON string into a Map
            val resultMapType = object : TypeToken<Map<String, Any>>() {}.type
            val resultMap: Map<String, Any> = Gson().fromJson(result.toString(), resultMapType)
            resultMap

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    private fun callPythonSearchSuggestion(inputText: String): List<VideosListData>? {
        return try {

            val mainFile = pythonInstant.getModule("main")
            val getResultFromPython = mainFile["Searcher"]?.call(inputText).toString()
            val videosListDataListType = object : TypeToken<List<VideosListData>>() {}.type
            Gson().fromJson(getResultFromPython, videosListDataListType)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

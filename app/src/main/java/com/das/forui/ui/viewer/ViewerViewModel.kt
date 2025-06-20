package com.das.forui.ui.viewer

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.forui.objectsAndData.Youtuber.formatDate
import com.das.forui.objectsAndData.Youtuber.formatViews
import com.das.forui.objectsAndData.Youtuber.pythonInstant
import com.das.forui.objectsAndData.ForUIDataClass.VideoDetails
import com.das.forui.objectsAndData.ForUIDataClass.VideosListData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ViewerViewModel : ViewModel() {
    private val _videoUrl = mutableStateOf("")
    val videoUrl: State<String> = _videoUrl

    private val _error = mutableStateOf("")
    val error: State<String> = _error

    private var _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private var _isLoadings = mutableStateOf(true)
    val isLoadings: State<Boolean> = _isLoadings

    private val _videoDetails = MutableLiveData<VideoDetails>()
    val videoDetails: LiveData<VideoDetails> = _videoDetails

    private val _searchResults = mutableStateOf<List<VideosListData>>(emptyList())
    val searchResults: State<List<VideosListData>> = _searchResults
    private val _isLoadingVideos = mutableStateOf(true)

    val isLoadingVideos: State<Boolean> = _isLoadingVideos


    fun loadVideoUrl(videoId: String) {
        viewModelScope.launch {
            try {

                val variable = pythonInstant["get_video_url"]
                val result = withContext(Dispatchers.IO){
                    variable?.call("https://www.youtube.com/watch?v=$videoId").toString()
                }

                if (result != "False") {
                    _videoUrl.value = result

                } else {
                    _error.value = "Please check your internet connection"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun fetchVideoDetails(videoId: String) {
        viewModelScope.launch {
            _isLoadings.value = true

            try {
                val videoDetails = withContext(Dispatchers.IO) {
                    callPythonSearchWithLink(videoId)
                }

                if (videoDetails != null) {
                    _videoDetails.postValue(
                        VideoDetails(
                            title = videoDetails.title,
                            viewNumber = formatViews(videoDetails.viewNumber.toLong()),
                            date = formatDate(videoDetails.date),
                            channelName = videoDetails.channelName,
                            description = videoDetails.description
                        )
                    )
                    println("here is one _1: $videoDetails \n also ${_videoDetails.value}")
                } else {
                    _error.value = "Failed to fetch video details"

                }
            } catch (e: Exception) {
                _error.value = "Error fetching video details: ${e.message}"

            } finally {
                _isLoadings.value = false

            }
        }
    }


    fun fetchSuggestions(title: String) {
        viewModelScope.launch {
            _isLoadingVideos.value = true
            val result = withContext(Dispatchers.IO){
                callPythonSearchSuggestion(title)
            }

            _searchResults.value = result ?: emptyList()
            _isLoadingVideos.value = false

        }
    }

    private fun callPythonSearchWithLink(inputText: String): VideoDetails? {
        return try {
            val variable = pythonInstant["SearchWithLink"]?.call("https://www.youtube.com/watch?v=$inputText")
            val result = variable.toString()
            println("python: $result")

            // Use Gson to parse the JSON string into a Map
            val resultMapType = object : TypeToken<VideoDetails>() {}.type
            val resultMap:VideoDetails = Gson().fromJson(result, resultMapType)
            resultMap

        }catch (e: JsonSyntaxException){
            Log.e("JSON Error", "Error parsing JSON ${e.message}")
            return null
        }
        catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    private fun callPythonSearchSuggestion(inputText: String): List<VideosListData>? {
        return try {

            val getResultFromPython = pythonInstant["Searcher"]?.call(inputText).toString()

            val videosListDataListType = object : TypeToken<List<VideosListData>>() {}.type
            val result: List<VideosListData>? = Gson().fromJson(getResultFromPython, videosListDataListType)
            result

        } catch (e: JsonSyntaxException) {
            Log.e("JSON Error", "Error parsing JSON: ${e.message}")
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

package com.das.mediaHub.ui.players.videoPlayer

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.YouTuber.formatDate
import com.das.mediaHub.data.YouTuber.formatViews
import com.das.mediaHub.data.YouTuber.pythonInstant
import com.das.mediaHub.data.model.VideoDetails
import com.das.mediaHub.data.model.searcher.SearchResponse
import com.das.mediaHub.data.model.searcher.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json


class ViewerViewModel : ViewModel() {

    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val _videoUrl = mutableStateOf("")
    val videoUrl: State<String> = _videoUrl

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private var _isLoadings = mutableStateOf(true)
    val isLoadings: State<Boolean> = _isLoadings

    private val _videoDetails = mutableStateOf<VideoDetails?>(null)
    val videoDetails: State<VideoDetails?> = _videoDetails

    private val _searchResults = mutableStateOf<List<Video>>(emptyList())
    val searchResults: State<List<Video>> = _searchResults

    private val _isSuggestionError = mutableStateOf<String?>(null)
    val isSuggestionError: State<String?> = _isSuggestionError

    private val _isLoadingVideos = mutableStateOf(true)
    val isLoadingVideos: State<Boolean> = _isLoadingVideos


    fun loadVideoUrl(videoId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val python = pythonInstant.getModule("main")
                val result = withContext(Dispatchers.IO) {
                    python["get_video_url"]?.call("https://www.youtube.com/watch?v=$videoId").toString()
                }

                if (result != "False") {
                    _videoUrl.value = result
                } else {
                    _error.value = "Something went wrong Please check your internet connection"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun fetchVideoDetails(videoId: String) {
        _isLoadings.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val videoDetails = callPythonSearchWithLink(videoId)

                _videoDetails.value = videoDetails.copy(
                    viewNumber = formatViews(videoDetails.viewNumber.toLong()),
                    date = formatDate(videoDetails.date)
                )
            } catch (js: SerializationException) {
                _error.value = "Error while fetching data: ${js.message}"
                Log.e("VideoPlayer", "Error fetching json video details: ${js.message}")
            } catch (e: Exception) {
                _error.value = "Error loading video details: ${e.message}"
                Log.e("VideoPlayer", "Error loading video details: ${e.message}")
            } finally {
                _isLoadings.value = false

            }
        }
    }


    fun fetchSuggestions(title: String) {
        _isLoadingVideos.value = true
        _isSuggestionError.value = null
        viewModelScope.launch {
            try {
                val result = callPythonSearchSuggestion(title)
                _searchResults.value = result
            } catch (j: SerializationException) {
                _isSuggestionError.value = "Error parsing data: ${j.localizedMessage}"
                Log.e("VideoPlayer", "Error parsing data: ${j.localizedMessage}")
            } catch (e: Exception) {
                _isSuggestionError.value = "Something went wrong: ${e.message}"
                Log.e("VideoPlayer", "Something went wrong: ${e.message}")
            } finally {
                _isLoadingVideos.value = false
            }

        }
    }

    private suspend fun callPythonSearchWithLink(inputText: String): VideoDetails {
        return try {
            val python = pythonInstant.getModule("main")

            val result = withContext(Dispatchers.IO) {
                python["SearchWithLink"]?.call("https://www.youtube.com/watch?v=$inputText").toString()
            }

            // Use Json to parse the JSON string into a Map
            jsonParser.decodeFromString<VideoDetails>(result)

        } catch (e: SerializationException) {
            Log.e("Serialization Error", "Error parsing JSON ${e.message}")
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


    private suspend fun callPythonSearchSuggestion(inputText: String): List<Video> {

        return try {
            val python = pythonInstant.getModule("main")
            val getResultFromPython = withContext(Dispatchers.IO) {
                python["Searcher"]?.call(inputText).toString()
            }
            val result = jsonParser.decodeFromString<SearchResponse>(getResultFromPython)
            result.result
        } catch (e: SerializationException) {
            Log.e("Serialization Error", "Error parsing JSON ${e.message}")
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}

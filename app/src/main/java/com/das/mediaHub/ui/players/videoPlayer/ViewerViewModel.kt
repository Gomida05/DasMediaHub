package com.das.mediaHub.ui.players.videoPlayer

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.python.YouTuber.formatDate
import com.das.mediaHub.python.YouTuber.formatViews
import com.das.mediaHub.data.model.VideoDetails
import com.das.mediaHub.data.model.responds.RespondVideoDetails
import com.das.mediaHub.data.model.responds.ResponseVideo
import com.das.mediaHub.data.model.searcher.Video
import com.das.mediaHub.python.PythonMain.callMethod
import com.das.mediaHub.python.PythonMain.getStreamUrl
import com.das.mediaHub.python.PythonMain.pythonInstant
import com.das.mediaHub.python.data.Names.GET_VIDEO_STREAM_URL
import com.das.mediaHub.python.data.Names.SEARCHER
import com.das.mediaHub.python.data.Names.SEARCH_WITH_URL
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException


class ViewerViewModel : ViewModel() {

    private val _videoUrl = mutableStateOf("")
    val videoUrl: State<String> = _videoUrl

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isLoadings = mutableStateOf(false)
    val isLoadings: State<Boolean> = _isLoadings

    private val _errorVideoDetails = mutableStateOf<String?>(null)
    val errorVideoDetails: State<String?> = _errorVideoDetails

    private val _videoDetails = mutableStateOf<VideoDetails?>(null)
    val videoDetails: State<VideoDetails?> = _videoDetails

    private val _searchResults = mutableStateOf<List<Video>>(emptyList())
    val searchResults: State<List<Video>> = _searchResults

    private val _isSuggestionError = mutableStateOf<String?>(null)
    val isSuggestionError: State<String?> = _isSuggestionError

    private val _isLoadingVideos = mutableStateOf(false)
    val isLoadingVideos: State<Boolean> = _isLoadingVideos

    fun loadDetails(videoId: String) {
        loadVideoUrl(videoId)
        fetchVideoDetails(videoId)
    }

    fun loadVideoUrl(videoId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = getStreamUrl(type = GET_VIDEO_STREAM_URL, id = videoId)

                if (result.success && !result.result.isNullOrEmpty()) {
                    _videoUrl.value = result.result
                } else {
                    _error.value = result.error
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
        _errorVideoDetails.value = null
        viewModelScope.launch {
            try {
                val result = callPythonSearchWithLink(videoId)
                if (result.success && result.result != null) {
                    val videoDetails = result.result
                    _videoDetails.value = videoDetails.copy(
                        viewNumber = videoDetails.viewNumber.formatViews(),
                        date = videoDetails.date.formatDate()
                    )
                } else {
                    _errorVideoDetails.value = result.error ?: "Something went wrong!"
                }
            } catch (js: SerializationException) {
                _errorVideoDetails.value = "Error while fetching data: ${js.message}"
                Log.e("VideoPlayer", "Error fetching json video details: ${js.message}")
            } catch (e: Exception) {
                _errorVideoDetails.value = "Error loading video details: ${e.message}"
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
                if (result.success && result.result != null) {
                    _searchResults.value = result.result.result
                } else {
                    _isSuggestionError.value = result.error ?: "Something went wrong!"
                }
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

    private suspend fun callPythonSearchWithLink(inputText: String): RespondVideoDetails {
        val result = pythonInstant.callMethod<RespondVideoDetails>(
                name = SEARCH_WITH_URL,
                args = "https://www.youtube.com/watch?v=$inputText"
        )

        return result
    }


    private suspend fun callPythonSearchSuggestion(inputText: String): ResponseVideo {
        return pythonInstant.callMethod<ResponseVideo>(SEARCHER, inputText)
    }

}
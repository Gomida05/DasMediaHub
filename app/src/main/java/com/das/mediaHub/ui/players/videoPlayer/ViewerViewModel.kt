package com.das.mediaHub.ui.players.videoPlayer

import android.util.Log
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
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException


class ViewerViewModel : ViewModel() {


    private val _videoState = MutableStateFlow(UiState<String>())
    val videoState = _videoState.asStateFlow()


    private val _detailsState = MutableStateFlow(UiState<VideoDetails>())
    val detailsState = _detailsState.asStateFlow()

    private val _suggestionsState = MutableStateFlow(UiState<List<Video>>())
    val suggestionsState = _suggestionsState.asStateFlow()


    fun loadDetails(videoId: String) {
        loadVideoUrl(videoId)
        fetchVideoDetails(videoId)
    }

    fun loadVideoUrl(videoId: String) {
        _videoState.value.copy(
            isLoading = true,
            error = null
        )
        viewModelScope.launch {
            try {
                val result = getStreamUrl(type = GET_VIDEO_STREAM_URL, id = videoId)

                if (result.success && !result.result.isNullOrEmpty()) {
                    _videoState.value.copy(
                        data = result.result
                    )
                } else {
                    _videoState.value.copy(
                        error = result.error
                    )
                }
            } catch (e: Exception) {
                _videoState.value.copy(
                    error = "Error: ${e.message}"
                )
            } finally {
                _videoState.value.copy(
                    isLoading = false
                )
            }
        }
    }


    fun fetchVideoDetails(videoId: String) {
        _detailsState.value.copy(
            isLoading = true,
            error = null
        )
        viewModelScope.launch {
            try {
                val result = callPythonSearchWithLink(videoId)
                if (result.success && result.result != null) {
                    val videoDetails = result.result
                    _detailsState.value.copy(
                        data = videoDetails.copy(
                            viewNumber = videoDetails.viewNumber.formatViews(),
                            date = videoDetails.date.formatDate()
                        )
                    )
                } else {
                    _detailsState.value.copy(
                        error = result.error ?: "Something went wrong!"
                    )
                }
            } catch (js: SerializationException) {
                _detailsState.value.copy(
                    error = "Error while fetching data: ${js.message}"
                )
                Log.e("VideoPlayer", "Error fetching json video details: ${js.message}")
            } catch (e: Exception) {
                _detailsState.value.copy(
                    error = "Error loading video details: ${e.message}"
                )
                Log.e("VideoPlayer", "Error loading video details: $e")
            } finally {
                _detailsState.value.copy(
                    isLoading = false
                )
            }
        }
    }


    fun fetchSuggestions(title: String) {
        _suggestionsState.value.copy(
            isLoading = true,
            error = null
        )
        viewModelScope.launch {
            try {
                val result = callPythonSearchSuggestion(title)
                if (result.success && result.result != null) {
                    _suggestionsState.value.copy(
                        data = result.result.result
                    )
                } else {
                    _suggestionsState.value.copy(
                        error = result.error ?: "Something went wrong!"
                    )
                }
            } catch (j: SerializationException) {
                Log.e("VideoPlayer", "Error parsing data: ${j.localizedMessage}")
            } catch (e: Exception) {
                _suggestionsState.value.copy(
                    error = "Something went wrong: ${e.message}"
                )
                Log.e("VideoPlayer", "Something went wrong: ${e.message}")
            } finally {
                _suggestionsState.value.copy(
                    isLoading = false
                )
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
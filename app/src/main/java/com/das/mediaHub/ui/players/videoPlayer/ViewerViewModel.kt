package com.das.mediaHub.ui.players.videoPlayer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.constants.GlobalVideoList
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import com.das.python.YouTuber
import com.das.python.YouTuber.formatDate
import com.das.python.YouTuber.formatViews
import com.das.python.data.model.FewVideoDetails
import com.das.python.data.model.searcher.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException


class ViewerViewModel : ViewModel() {


    private val _currentVideoMeta = MutableStateFlow<Pair<String?, String?>?>(null)
    val currentVideoMeta = _currentVideoMeta.asStateFlow()

    private val _videoState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val videoState = _videoState.asStateFlow()


    private val _detailsState = MutableStateFlow<UiState<FewVideoDetails>>(UiState.Idle)
    val detailsState = _detailsState.asStateFlow()

    private val _suggestionsState = MutableStateFlow<UiState<List<Video>>>(UiState.Idle)
    val suggestionsState = _suggestionsState.asStateFlow()


    fun loadDetails(videoId: String) {
        loadVideoUrl(videoId)
        fetchVideoDetails(videoId)
    }

    fun loadVideoUrl(videoId: String) {
        _videoState.value = UiState.Loading

        viewModelScope.launch {
            try {
                YouTuber.getVideoStreamUrl(videoId = videoId,
                    onSuccess = { newResult->
                        _videoState.value = UiState.Success(newResult)
                    },
                    onFailure = { error->
                        _videoState.value = UiState.Error(error)
                    }
                )
            } catch (e: Exception) {
                _videoState.value = UiState.Error(e.message ?: "Something went wrong!")
            }
        }
    }


    fun fetchVideoDetails(videoId: String) {
        _detailsState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val result = YouTuber.searchByUrl(url = "https://www.youtube.com/watch?v=$videoId")
                val videoDetails = result.result
                if (result.success && videoDetails != null) {
                    _detailsState.value = UiState.Success(
                        data = videoDetails.copy(
                            viewNumber = videoDetails.viewNumber.formatViews(),
                            date = videoDetails.date.formatDate()
                        )
                    )
                } else {
                    _detailsState.value = UiState.Error(message = result.error ?: "Something went wrong!")
                }
            } catch (js: SerializationException) {
                _detailsState.value = UiState.Error(message = "Error fetching json video details: ${js.message}")
                Log.e("VideoPlayer", "Error fetching json video details: ${js.message}")
            } catch (e: Exception) {
                _detailsState.value = UiState.Error(message = "Error loading video details: ${e.message}")
                Log.e("VideoPlayer", "Error loading video details: $e")
            }
        }
    }


    fun fetchSuggestions(
        videoId: String,
        title: String
    ) {
        _suggestionsState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val result = YouTuber.search(title)
                val videoDetails = result.result
                if (result.success && videoDetails != null) {
                    val item = videoDetails.result.firstOrNull { it.id == videoId }
                    _currentVideoMeta.value = item?.channel?.thumbnails?.firstOrNull()?.url to item?.duration
                    val filtered = videoDetails.result
                        .asSequence()
                        .filterNot { it.id == videoId }
                        .distinctBy { it.id }
                        .toList()

                    if (filtered.isEmpty()) {
                        GlobalVideoList.clear()
                        _suggestionsState.value = UiState.Empty
                    } else {
                        GlobalVideoList.setVideos(filtered)
                        _suggestionsState.value = UiState.Success(data = filtered)
                    }
                } else {
                    _suggestionsState.value = UiState.Error(message = result.error ?: "Something went wrong!")
                }
            } catch (j: SerializationException) {
                _suggestionsState.value = UiState.Error(message = "Something went wrong: ${j.message}")

                Log.e("VideoPlayer", "Error parsing data: ${j.localizedMessage}")
            } catch (e: Exception) {
                _suggestionsState.value = UiState.Error(message = "Something went wrong: ${e.message}")
                Log.e("VideoPlayer", "Something went wrong: ${e.message}")
            }

        }
    }



}
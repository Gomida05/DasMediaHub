package com.das.mediaHub.ui.players.videoPlayer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.constants.GlobalVideoList
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.data.model.state.VideoUiState
import com.das.mediaHub.data.model.state.VideoUiState.Companion.toVideoUiState
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

    private val _videoState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val videoState = _videoState.asStateFlow()

    private val _detailsState = MutableStateFlow<UiState<FewVideoDetails>>(UiState.Idle)
    val detailsState = _detailsState.asStateFlow()

    private val _suggestionsState = MutableStateFlow<UiState<List<Video>>>(UiState.Idle)
    val suggestionsState = _suggestionsState.asStateFlow()

    private val _videoUiState = MutableStateFlow(VideoUiState.EMPTY)
    val videoUiState = _videoUiState.asStateFlow()

    fun initialize(videoId: String) {
        setCachedVideo(videoId)
        loadVideoUrl(videoId)
        loadVideoDetails(videoId)
    }

    private fun setCachedVideo(videoId: String) {
        val cachedVideo = GlobalVideoList.getVideoById(videoId)
        _videoUiState.value = cachedVideo?.toVideoUiState() ?: VideoUiState.EMPTY
    }

    private fun loadVideoUrl(videoId: String) {
        _videoState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val streamUrl = YouTuber.getVideoStreamUrl(videoId)
                _videoState.value = if (streamUrl.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(streamUrl)
                }
            } catch (e: Exception) {
                _videoState.value = UiState.Error(ErrorMapper.map(e))
            }
        }
    }

    private fun loadVideoDetails(videoId: String) {
        _detailsState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val result = YouTuber.searchByUrl("https://www.youtube.com/watch?v=$videoId")
                val details = result.result

                if (!result.success || details == null) {
                    _detailsState.value = UiState.Error(
                        message = ErrorMapper.mapMessage(result.error)
                    )
                    return@launch
                }

                val formattedDetails = details.copy(
                    viewNumber = details.viewNumber.formatViews(),
                    date = details.date.formatDate()
                )

                _detailsState.value = UiState.Success(formattedDetails)

                _videoUiState.value = _videoUiState.value.copy(
                    title = formattedDetails.title,
                    views = formattedDetails.viewNumber,
                    date = formattedDetails.date,
                    channelName = formattedDetails.channelName
                )

                if (formattedDetails.title.isNotEmpty()) {
                    fetchSuggestions(videoId, formattedDetails.title)
                }
            } catch (e: SerializationException) {
                _detailsState.value = UiState.Error(
                    message = ErrorMapper.mapMessage(e.message)
                )
                Log.e("VideoPlayer", "Error parsing video details JSON: ${e.message}")
            } catch (e: Exception) {
                _detailsState.value = UiState.Error(message = ErrorMapper.map(e))
                Log.e("VideoPlayer", "Error loading video details: ${e.message}", e)
            }
        }
    }

    fun fetchSuggestions(videoId: String, title: String) {
        _suggestionsState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val result = YouTuber.search(title)
                val searchResult = result.result

                if (!result.success || searchResult == null) {
                    _suggestionsState.value = UiState.Error(
                        message = ErrorMapper.mapMessage(result.error)
                    )
                    return@launch
                }

                val currentVideo = searchResult.result.firstOrNull { it.id == videoId }

                _videoUiState.value = _videoUiState.value.copy(
                    channelThumbnail = currentVideo?.channel?.thumbnails?.firstOrNull()?.url,
                    duration = currentVideo?.duration
                )

                val suggestions = searchResult.result
                    .asSequence()
                    .filterNot { it.id == videoId }
                    .distinctBy { it.id }
                    .toList()

                if (suggestions.isEmpty()) {
                    GlobalVideoList.clear()
                    _suggestionsState.value = UiState.Empty
                } else {
                    GlobalVideoList.setVideos(suggestions)
                    _suggestionsState.value = UiState.Success(suggestions)
                }
            } catch (e: SerializationException) {
                _suggestionsState.value = UiState.Error(message = ErrorMapper.map(e))
                Log.e("VideoPlayer", "Error parsing suggestions: ${e.localizedMessage}")
            } catch (e: Exception) {
                _suggestionsState.value = UiState.Error(message = ErrorMapper.map(e))
                Log.e("VideoPlayer", "Error loading suggestions: ${e.message}", e)
            }
        }
    }
}
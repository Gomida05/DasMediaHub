package com.das.mediaHub.ui.players.videoPlayer

import android.net.Uri
import android.util.Log
import android.util.Xml
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.das.mediaHub.data.constants.GlobalVideoList
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.error.ErrorMapper.MSG_GENERIC
import com.das.mediaHub.data.mediacontroller.online.VideoPlayerListener
import com.das.mediaHub.data.mediacontroller.online.VideoPlayerManager
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.data.model.state.VideoPlayerState
import com.das.mediaHub.data.model.state.VideoUiState
import com.das.mediaHub.data.model.state.VideoUiState.Companion.toVideoUiState
import com.das.mediaHub.data.repository.FavoritesRepository
import com.das.mediaHub.data.repository.WatchHistoryRepository
import com.das.python.YouTuber
import com.das.python.YouTuber.formatDate
import com.das.python.YouTuber.formatViews
import com.das.python.YouTuber.loadStreamUrl
import com.das.python.data.model.ItemsStreamUrlsForMediaItemData
import com.das.python.data.model.VideosListData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val historyRepository: WatchHistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val videoPlayerManager: VideoPlayerManager
): ViewModel() {


    private val playerListener = VideoPlayerListener {
        playNextVideo()
    }


    // 2. Register the listener when the ViewModel is created
    init {
        videoPlayerManager.addListener(playerListener)
    }

    val currentPosition: Long
        get() = videoPlayerManager.currentPosition
    val player: Player
        get() = videoPlayerManager.player

    private val _uiState = MutableStateFlow(VideoPlayerState())
    val uiState = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val isSaved: StateFlow<Boolean> = _uiState
        .map { it.videoId } // 1. Listen specifically to the videoId
        .distinctUntilChanged() // 2. Only run the query if the ID actually changes
        .flatMapLatest { videoId ->
            // 3. Switch to the Room database flow for the current ID
            if (videoId.isEmpty()) {
                flowOf(false) // Return false immediately if there is no ID yet
            } else {
                favoritesRepository.isWatchUrlExist(videoId)
            }
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * Initializes the player with a video ID.
     * Updates metadata instantly from cache if available.
     */
    fun loadVideo(videoId: String) {
        if (_uiState.value.videoId == videoId &&
            _uiState.value.streamState is UiState.Success &&
            _uiState.value.detailsState is UiState.Success
        ) return

        // Update basic info immediately from cache
        val cachedVideo = GlobalVideoList.getVideoById(videoId)

        _uiState.update {
            it.copy(
                videoId = videoId,
                streamState = UiState.Loading,
                detailsState = UiState.Loading,
                metadata = cachedVideo?.toVideoUiState() ?: VideoUiState.EMPTY,
                description = ""
            )
        }

        fetchStreamUrl(videoId)
        fetchVideoDetails(videoId)
    }

    /**
     * Refreshes the current video data.
     */
    fun refresh() {
        loadVideo(_uiState.value.videoId)
    }

    private fun fetchStreamUrl(videoId: String) {
        viewModelScope.launch {
            try {
                val streamUrl = YouTuber.getVideoStreamUrl(videoId)
                _uiState.update { state ->
                    state.copy(
                        streamState = if (streamUrl.isEmpty()) UiState.Empty else UiState.Success(
                            streamUrl
                        )
                    )
                }
            } catch (e: Exception) {
                val errorMessage = ErrorMapper.map(e)
                val suggestionState = (_uiState.value.suggestionsState as? UiState.Error)?.message

                _uiState.update {
                    it.copy(streamState = UiState.Error(
                        if (errorMessage == MSG_GENERIC && suggestionState != null) {
                            suggestionState
                        } else {
                            errorMessage
                        }

                    ))
                }
            }
        }
    }

    private fun fetchVideoDetails(videoId: String) {
        viewModelScope.launch {
            try {
                val result = YouTuber.searchByUrl("https://www.youtube.com/watch?v=$videoId")
                val details = result.result

                if (!result.success || details == null) {
                    _uiState.update {
                        it.copy(detailsState = UiState.Error(ErrorMapper.mapMessage(result.error)))
                    }
                    return@launch
                }

                val formattedDetails = details.copy(
                    viewNumber = details.viewNumber.formatViews(),
                    date = details.date.formatDate()
                )

                val decodedDescription = try {
                    URLDecoder.decode(details.description, Xml.Encoding.UTF_8.name)
                } catch (_: Exception) {
                    details.description
                }.trim().ifBlank { "No description available." }

                _uiState.update { state ->
                    state.copy(
                        detailsState = UiState.Success(formattedDetails),
                        description = decodedDescription,
                        metadata = state.metadata.copy(
                            title = formattedDetails.title,
                            views = formattedDetails.viewNumber,
                            date = formattedDetails.date,
                            channelName = formattedDetails.channelName
                        )
                    )
                }

                if (formattedDetails.title.isNotEmpty()) {
                    fetchSuggestions(videoId, formattedDetails.title)
                }
            } catch (e: SerializationException) {
                _uiState.update { it.copy(detailsState = UiState.Error(ErrorMapper.mapMessage(e.message))) }
                Log.e("VideoPlayer", "Error parsing video details JSON: ${e.message}")
            } catch (e: Exception) {
                _uiState.update { it.copy(detailsState = UiState.Error(ErrorMapper.map(e))) }
                Log.e("VideoPlayer", "Error loading video details: ${e.message}", e)
            }
        }
    }

    fun loadStreamForBackGroud(
        onStart: () -> Unit,
        onSuccess: (ItemsStreamUrlsForMediaItemData) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        onStart()
        viewModelScope.launch {
            val currentState = _uiState.value
            val metadata = currentState.metadata
            val mediaDetails = VideosListData(
                    videoId = _uiState.value.videoId,
                    title = metadata.title.orEmpty(),
                    dateOfVideo = metadata.date.orEmpty(),
                    views = metadata.views.orEmpty(),
                    channelName = metadata.channelName.orEmpty(),
                    duration = metadata.duration.orEmpty(),
                    channelThumbnailsUrl = metadata.channelThumbnail.orEmpty()
                )
            mediaDetails.loadStreamUrl(
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        }
    }

    fun fetchSuggestions(videoId: String, title: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(suggestionsState = UiState.Loading) }
            try {
                val result = YouTuber.search(title)
                val searchResult = result.result

                if (!result.success || searchResult == null) {
                    _uiState.update {
                        it.copy(suggestionsState = UiState.Error(ErrorMapper.mapMessage(result.error)))
                    }
                    return@launch
                }

                val currentVideo = searchResult.result.firstOrNull { it.id == videoId }

                _uiState.update { state ->
                    state.copy(
                        metadata = state.metadata.copy(
                            channelThumbnail = currentVideo?.channel?.thumbnails?.firstOrNull()?.url,
                            duration = currentVideo?.duration
                        )
                    )
                }

                val suggestions = searchResult.result
                    .asSequence()
                    .filterNot { it.id == videoId }
                    .distinctBy { it.id }
                    .toList()

                if (suggestions.isEmpty()) {
                    GlobalVideoList.clear()
                    _uiState.update { it.copy(suggestionsState = UiState.Empty) }
                } else {
                    GlobalVideoList.setVideos(suggestions)
                    _uiState.update { it.copy(suggestionsState = UiState.Success(suggestions)) }
                }
            } catch (e: SerializationException) {
                _uiState.update { it.copy(suggestionsState = UiState.Error(ErrorMapper.map(e))) }
                Log.e("VideoPlayer", "Error parsing suggestions: ${e.localizedMessage}")
            } catch (e: Exception) {
                _uiState.update { it.copy(suggestionsState = UiState.Error(ErrorMapper.map(e))) }
                Log.e("VideoPlayer", "Error loading suggestions: ${e.message}", e)
            }
        }
    }


    fun addHistory() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val metadata = currentState.metadata
            historyRepository.insertNewVideo(
                videoId = currentState.videoId,
                title = metadata.title.orEmpty(),
                videoDate = metadata.date.orEmpty(),
                videoViewCount = metadata.views.orEmpty(),
                videoChannelName = metadata.channelName.orEmpty(),
                duration = metadata.duration.orEmpty(),
                channelThumbnail = metadata.channelThumbnail.orEmpty()
            )
        }
    }

    fun addToFavDb() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val metadata = currentState.metadata
            favoritesRepository.insertData(
                videoId = currentState.videoId,
                title = metadata.title.orEmpty(),
                videoDate = metadata.date.orEmpty(),
                videoViewCount = metadata.views.orEmpty(),
                videoChannelName = metadata.channelName.orEmpty(),
                duration = metadata.duration.orEmpty(),
                channelThumbnail = metadata.channelThumbnail.orEmpty()
            )
        }
    }
    fun deleteFromFavDb() {
        viewModelScope.launch {
            favoritesRepository.deleteWatchUrl(_uiState.value.videoId)
        }
    }

    fun playVideo(videoId: String, uri: Uri) {
        videoPlayerManager.playVideo(videoId = videoId, uri = uri)
    }

    fun closeCurrentlyPlayingMedia() {
        videoPlayerManager.closeCurrentlyMedia()
    }

    fun pause() {
        videoPlayerManager.pause()
    }




    private fun playNextVideo() {
        // Fetch the next ID from your global list
        GlobalVideoList.getVideoAt(0)?.id?.let { nextId ->
            // Update the state and fetch new URLs
            loadVideo(nextId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        videoPlayerManager.removeListener(playerListener)
        closeCurrentlyPlayingMedia()
    }

}
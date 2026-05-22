package com.das.mediaHub.ui.watchedVideos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.WatchedVideoEntity
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.data.repository.WatchHistoryRepository
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.toVideosListData
import com.das.python.YouTuber.loadStreamUrl
import com.das.python.data.model.ItemsStreamUrlsForMediaItemData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchedVideosViewModel @Inject constructor(
    private val repository: WatchHistoryRepository
) : ViewModel() {
    var searchQuery = MutableStateFlow("")
        private set

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState = searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            repository.getWatchedVideos()
                .map { list ->
                    val filtered = if (query.isBlank()) {
                        list
                    } else {
                        list.filter {
                            it.title.contains(query, ignoreCase = true) ||
                                    it.channelName.contains(query, ignoreCase = true)
                        }
                    }

                    if (filtered.isEmpty()) UiState.Empty
                    else UiState.Success(filtered)
                }
        }
        .onStart {
            emit(UiState.Loading)
        }
        .catch {
            emit(UiState.Error(ErrorMapper.map(it)))
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Idle
        )


    fun deleteVideo(videoId: String) {
        viewModelScope.launch {
            repository.deleteWatchUrl(videoId)
        }
    }


    fun searchVideos(query: String) {
        searchQuery.value = query
    }

    fun clearAllVideos() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun loadStreamUrl(
        mediaItem: WatchedVideoEntity,
        onStart: () -> Unit,
        onSuccess: (ItemsStreamUrlsForMediaItemData) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        onStart()
        viewModelScope.launch {
            mediaItem.toVideosListData().loadStreamUrl(
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        }
    }
}
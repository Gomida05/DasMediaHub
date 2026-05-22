package com.das.mediaHub.ui.watch_later

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.SavedVideosListData
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.data.repository.FavoritesRepository
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.toVideosListData
import com.das.python.YouTuber.loadStreamUrl
import com.das.python.data.model.ItemsStreamUrlsForMediaItemData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedVideosViewModel @Inject constructor(
    private val dbHelper: FavoritesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            dbHelper.allSavedVideos
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
                .onStart {
                    // Emits a loading state inside the stream every time the query parameters update
                    emit(UiState.Loading)
                }
        }
        .catch { e -> emit(UiState.Error(ErrorMapper.map(e))) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Idle
        )

    fun searchVideos(query: String) {
        _searchQuery.value = query
    }

    fun deleteVideo(videoId: String) {
        viewModelScope.launch {
            runCatching {
                dbHelper.deleteWatchUrl(videoId)
            }.onFailure { e ->
                _errorFlow.tryEmit(e.message ?: "Failed to delete video")
            }
        }
    }

    fun clearAllVideos() {
        viewModelScope.launch {
            runCatching {
                dbHelper.clearAll()
            }.onFailure { e ->
                _errorFlow.tryEmit(e.message ?: "Failed to clear repository")
            }
        }
    }

    fun loadStreamUrl(
        mediaItem: SavedVideosListData,
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
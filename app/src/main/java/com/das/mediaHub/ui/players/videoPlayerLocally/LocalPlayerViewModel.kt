package com.das.mediaHub.ui.players.videoPlayerLocally

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.local.LocalMediaDataSource
import com.das.mediaHub.data.model.interfaces.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class LocalPlayerViewModel @Inject constructor(
    private val mediaDataSource: LocalMediaDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<MediaItem>>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _currentMediaMetadata = MutableStateFlow<UiState<MediaMetadata>>(UiState.Idle)
    val currentMediaMetadata = _currentMediaMetadata.asStateFlow()

    private var lastScanPath: String? = null
    private var scanJob: Job? = null

    fun init(videoUri: String) {
        loadCurrentMediaInfo(videoUri.toUri())
        loadItemsDebounced(videoUri = videoUri)
    }

    private fun loadItemsDebounced(videoUri: String) {
        val parsedUri = videoUri.toUri()
        val pathLocation = resolveFolderPath(parsedUri, videoUri)

        if (pathLocation.isBlank()) {
            _uiState.value = UiState.Empty
            return
        }

        if (pathLocation == lastScanPath && _uiState.value is UiState.Success) return

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            delay(300.milliseconds)
            _uiState.value = UiState.Loading

            try {
                val items = mediaDataSource.scanFolder(videoUri, pathLocation)

                lastScanPath = pathLocation
                _uiState.value = when {
                    items.isEmpty() -> UiState.Empty
                    else -> UiState.Success(items)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(ErrorMapper.map(e))
                Log.d("LocalPlayerViewModel", "loadItemsDebounced: ${e.message}", e)
            }
        }
    }

    private fun resolveFolderPath(uri: Uri, rawValue: String): String {
        if (uri.scheme == "content") return ""

        val actualPath = when (uri.scheme) {
            "file" -> uri.path
            null, "" -> rawValue
            else -> uri.path
        }.orEmpty()

        return File(actualPath).parent.orEmpty()
    }

    private fun loadCurrentMediaInfo(uri: Uri) {
        viewModelScope.launch {
            _currentMediaMetadata.value = UiState.Loading

            try {
                val metadata = mediaDataSource.getMetadata(uri)
                _currentMediaMetadata.value = UiState.Success(metadata)
            } catch (e: Exception) {
                _currentMediaMetadata.value =
                    UiState.Error(ErrorMapper.map(e))
                Log.d("LocalPlayerViewModel", "loadCurrentMediaInfo: ${e.message}", e)
            }
        }
    }

}

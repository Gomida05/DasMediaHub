package com.das.mediaHub.ui.players.videoPlayerLocally

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LocalPlayerViewModel(
    private val resolver: ContentResolver
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
            delay(300)
            _uiState.value = UiState.Loading

            try {
                val items = withContext(Dispatchers.IO) {
                    scanFolder(videoUri, pathLocation)
                }

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

    private val cache = mutableMapOf<String, List<MediaItem>>()



    private fun scanFolder(
        currentMediaId: String,
        pathLocation: String
    ): List<MediaItem> {
        cache[pathLocation]?.let { return it }

        val dir = File(pathLocation)
        if (pathLocation.isBlank() || !dir.exists() || !dir.isDirectory) {
            Log.d("LocalPlayerViewModel", "scanFolder: invalid path = $pathLocation")
            return emptyList()
        }

        val items = dir.listFiles()
            ?.filter { file ->
                file.isFile && file.toUri().toString() != currentMediaId
            }
            ?.map { file ->
                MediaItem.Builder()
                    .setMediaId(file.toUri().toString())
                    .setUri(file.toUri())
                    .build()
            }
            ?: emptyList()

        cache[pathLocation] = items
        return items
    }

    private fun loadCurrentMediaInfo(uri: Uri) {
        viewModelScope.launch {
            _currentMediaMetadata.value = UiState.Loading

            try {
                val metadata = getCurrentMediaItemInfo(uri)

                _currentMediaMetadata.value = UiState.Success(metadata)
            } catch (e: Exception) {
                _currentMediaMetadata.value =
                    UiState.Error(ErrorMapper.map(e))
                Log.d("LocalPlayerViewModel", "loadCurrentMediaInfo: ${e.message}", e)
            }
        }
    }


    private suspend fun getCurrentMediaItemInfo(uri: Uri): MediaMetadata = withContext(Dispatchers.IO) {
        if (uri.scheme == "content") {
            getFromContentUri(uri)
        } else {
            getFromFileUri(uri)
        }
    }

    private fun getFromContentUri(uri: Uri): MediaMetadata {
        var displayName: String? = null
        var title: String? = null

        val projection = arrayOf(
            OpenableColumns.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE
        )

        resolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)

                if (displayNameIndex != -1) {
                    displayName = cursor.getString(displayNameIndex)
                }

                if (titleIndex != -1) {
                    title = cursor.getString(titleIndex)
                }
            }
        }

        val finalTitle = title
            ?: displayName
            ?: uri.lastPathSegment
            ?: "Unknown video"

        return MediaMetadata.Builder()
            .setTitle(finalTitle)
            .setSubtitle(displayName)
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .build()
    }

    private fun getFromFileUri(uri: Uri): MediaMetadata {
        val path = when {
            uri.scheme == "file" -> uri.path
            uri.scheme.isNullOrEmpty() -> uri.toString()
            else -> uri.path
        }.orEmpty()

        val file = File(path)
        val name = file.name.ifBlank { uri.lastPathSegment ?: "Unknown video" }

        return MediaMetadata.Builder()
            .setTitle(name)
            .setSubtitle(file.parentFile?.name)
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .build()
    }
}

package com.das.mediaHub.ui.downloaded

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.das.mediaHub.data.media.MediaStoreCache
import com.das.mediaHub.data.model.download.DownloadType
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DownloadedPageViewModel : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val _videoUiState = MutableStateFlow<UiState<List<MediaItem>>>(UiState.Idle)
    val videoUiState = _videoUiState.asStateFlow()

    private val _musicUiState = MutableStateFlow<UiState<List<MediaItem>>>(UiState.Idle)
    val musicUiState = _musicUiState.asStateFlow()

    private var videoJob: Job? = null
    private var musicJob: Job? = null


    fun fetchVideoFiles(pathLocation: String) {

        startLoading(type = DownloadType.VIDEO) {
            val result = loadMediaFiles(pathLocation, ".mp4", MediaMetadata.MEDIA_TYPE_VIDEO)

            _videoUiState.value = if (result.isEmpty()) UiState.Empty else UiState.Success(data = result)
            MediaStoreCache.updateMusicFiles(result)

        }
    }

    fun fetchMusicFiles(pathLocation: String) {

        startLoading(type = DownloadType.MUSIC) {
            val result = loadMediaFiles(pathLocation, ".mp3", MediaMetadata.MEDIA_TYPE_MUSIC)
            _musicUiState.value =
                if (result.isEmpty()) UiState.Empty else UiState.Success(data = result)
            MediaStoreCache.updateMusicFiles(result)
        }
    }

    private fun startLoading(type: DownloadType, block: suspend () -> Unit) {
        when (type) {
            DownloadType.VIDEO -> {
                videoJob?.cancel()
                _videoUiState.value = UiState.Loading
                videoJob = viewModelScope.launch(Dispatchers.IO) {
                    try {
                        block()
                    } catch (e: Exception) {
                        _videoUiState.value =
                            UiState.Error(e.message ?: "Unknown error occurred")
                    }
                }
            }

            DownloadType.MUSIC -> {
                musicJob?.cancel()
                _musicUiState.value = UiState.Loading
                musicJob = viewModelScope.launch(Dispatchers.IO) {
                    try {
                        block()
                    } catch (e: Exception) {
                        _musicUiState.value =
                            UiState.Error(e.message ?: "Unknown error occurred")
                    }
                }
            }

            else -> {}
        }
    }

    private fun loadMediaFiles(
        pathLocation: String,
        extension: String,
        mediaType: Int
    ): List<MediaItem> {
        val dir = File(pathLocation)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir.listFiles()
            ?.asSequence()
            ?.filter { it.isFile && it.name.endsWith(extension, ignoreCase = true) }
            ?.map { file ->
                val lastModified = file.lastModified()
                val formattedDate = formatDate(lastModified)
                val fileSize = formatFileSize(file.length())

                val metadata = MediaMetadata.Builder()
                    .setTitle(file.name.removeSuffix(extension))
                    .setDescription(formattedDate)
                    .setArtist(fileSize)
                    .setMediaType(mediaType)
                    .build()

                MediaItem.Builder()
                    .setMediaId(file.toUri().toString())
                    .setUri(file.toUri())
                    .setMediaMetadata(metadata)
                    .setTag(fileSize)
                    .build()
            }
            ?.distinctBy { it.mediaId }
            ?.sortedByDescending { File(it.mediaId).lastModified() }
            ?.toList() ?: emptyList()
    }


    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        return dateFormat.format(date)
    }


    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes >= 1_073_741_824 -> String.format(
                Locale.ROOT,
                "%.2f GB",
                sizeInBytes / 1_073_741_824.0
            )

            sizeInBytes >= 1_048_576 -> String.format(
                Locale.ROOT,
                "%.2f MB",
                sizeInBytes / 1_048_576.0
            )

            sizeInBytes >= 1_024 -> String.format(Locale.ROOT, "%.2f KB", sizeInBytes / 1_024.0)
            else -> String.format(Locale.ROOT, "%d bytes", sizeInBytes)
        }
    }

    fun deleteFileAndRefresh(filePath: String, isVideo: Boolean, pathLocation: String) {
        runCatching {
            File(filePath).delete()
        }

        if (isVideo) {
            fetchVideoFiles(pathLocation)
        } else {
            fetchMusicFiles(pathLocation)
        }
    }
}
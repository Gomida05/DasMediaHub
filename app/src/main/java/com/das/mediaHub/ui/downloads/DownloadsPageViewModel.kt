package com.das.mediaHub.ui.downloads

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.das.mediaHub.python.YouTuber.updateGlobalMediaItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DownloadsPageViewModel : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState = _uiState.asStateFlow()

    var currentJob: Job? = null

    private var cachedVideos: List<MediaItem>? = null
    private var cachedMusic: List<MediaItem>? = null

    fun fetchVideoFiles(pathLocation: String) {
        if (cachedVideos != null) {
            val videos = cachedVideos!!
            _uiState.value = _uiState.value.copy(videos = videos)
            videos.updateGlobalMediaItems()
            return
        }

        startLoading {
            val result = loadMediaFiles(pathLocation, ".mp4", MediaMetadata.MEDIA_TYPE_VIDEO)
            cachedVideos = result
            _uiState.value = _uiState.value.copy(videos = result)
            result.updateGlobalMediaItems()

        }
    }

    fun fetchMusicFiles(pathLocation: String) {
        if (cachedMusic != null) {
            val musics = cachedMusic!!
            _uiState.value = _uiState.value.copy(musics = musics)
            musics.updateGlobalMediaItems()
            return
        }

        startLoading {
            val result = loadMediaFiles(pathLocation, ".mp3", MediaMetadata.MEDIA_TYPE_MUSIC)
            cachedMusic = result
            _uiState.value = _uiState.value.copy(musics = result)
            result.updateGlobalMediaItems()
        }
    }

    private fun startLoading(block: suspend () -> Unit) {
        currentJob?.cancel()
        _uiState.value.copy(
            isLoading = true
        )
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                _uiState.value.copy(
                    error = "Something went wrong: ${e.message}"
                )
            } finally {
                _uiState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    private fun loadMediaFiles(pathLocation: String, extension: String, mediaType: Int): List<MediaItem> {
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
            ?.toList() ?: emptyList()
    }


    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        return dateFormat.format(date)
    }


    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes >= 1_073_741_824 -> String.format(Locale.ROOT, "%.2f GB", sizeInBytes / 1_073_741_824.0)
            sizeInBytes >= 1_048_576 -> String.format(Locale.ROOT, "%.2f MB", sizeInBytes / 1_048_576.0)
            sizeInBytes >= 1_024 -> String.format(Locale.ROOT, "%.2f KB", sizeInBytes / 1_024.0)
            else -> String.format(Locale.ROOT, "%d bytes", sizeInBytes)
        }
    }
}
package com.das.mediaHub.ui.downloads

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DownloadsPageViewModel : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val _videosListData = mutableStateOf<List<MediaItem>>(emptyList())
    val videosListData: State<List<MediaItem>> = _videosListData

    private val _musicListData = mutableStateOf<List<MediaItem>>(emptyList())
    val musicListData: State<List<MediaItem>> = _musicListData

    private val _loading = mutableStateOf(false)
    val isLoading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val errorFound: State<String?> = _error

    var currentJob: Job? = null

    private var cachedVideos: List<MediaItem>? = null
    private var cachedMusic: List<MediaItem>? = null

    fun fetchVideoFiles(pathLocation: String) {
        if (cachedVideos != null) {
            _videosListData.value = cachedVideos!!
            return
        }

        startLoading {
            val result = loadMediaFiles(pathLocation, ".mp4", MediaMetadata.MEDIA_TYPE_VIDEO)
            cachedVideos = result
            _videosListData.value = result
        }
    }

    fun fetchMusicFiles(pathLocation: String) {
        if (cachedMusic != null) {
            _musicListData.value = cachedMusic!!
            return
        }

        startLoading {
            val result = loadMediaFiles(pathLocation, ".mp3", MediaMetadata.MEDIA_TYPE_MUSIC)
            cachedMusic = result
            _musicListData.value = result
        }
    }

    private fun startLoading(block: suspend () -> Unit) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.value = true
                block()
            } catch (e: Exception) {
                _error.value = "Something went wrong: ${e.message}"
            } finally {
                _loading.value = false
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
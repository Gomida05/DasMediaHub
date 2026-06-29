package com.das.mediaHub.ui.downloaded

import android.content.Context
import android.media.MediaScannerConnection
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.mediacontroller.MediaStoreCache
import com.das.mediaHub.data.model.enums.ContentType
import com.das.mediaHub.data.model.interfaces.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DownloadedPageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val justExoPlayer: ExoPlayer
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val _videoUiState = MutableStateFlow<UiState<List<MediaItem>>>(UiState.Idle)
    val videoUiState = _videoUiState.asStateFlow()

    private val _musicUiState = MutableStateFlow<UiState<List<MediaItem>>>(UiState.Idle)
    val musicUiState = _musicUiState.asStateFlow()

    private var videoJob: Job? = null
    private var musicJob: Job? = null

    fun initialize(videoPath: String, audioPath: String) {
        fetchVideoFiles(pathLocation = videoPath)
        fetchMusicFiles(pathLocation = audioPath)
    }


    private fun fetchVideoFiles(pathLocation: String) {

        startLoading(type = ContentType.VIDEO) {
            val result = loadMediaFiles(pathLocation, it.extension, MediaMetadata.MEDIA_TYPE_VIDEO)

            _videoUiState.value = if (result.isEmpty()) UiState.Empty else UiState.Success(data = result)
            MediaStoreCache.updateVideosFiles(result)

        }
    }

    private fun fetchMusicFiles(pathLocation: String) {

        startLoading(type = ContentType.MUSIC) {
            val result = loadMediaFiles(pathLocation, it.extension, MediaMetadata.MEDIA_TYPE_MUSIC)
            _musicUiState.value =
                if (result.isEmpty()) UiState.Empty else UiState.Success(data = result)
            MediaStoreCache.updateMusicFiles(result)
        }
    }

    private fun startLoading(type: ContentType, block: suspend (ContentType) -> Unit) {
        when (type) {
            ContentType.VIDEO -> {
                videoJob?.cancel()
                _videoUiState.value = UiState.Loading
                videoJob = viewModelScope.launch(Dispatchers.IO) {
                    try {
                        block(type)
                    } catch (e: Exception) {
                        _videoUiState.value = UiState.Error(ErrorMapper.map(e))
                    }
                }
            }

            ContentType.MUSIC -> {
                musicJob?.cancel()
                _musicUiState.value = UiState.Loading
                musicJob = viewModelScope.launch(Dispatchers.IO) {
                    try {
                        block(type)
                    } catch (e: Exception) {
                        _musicUiState.value = UiState.Error(ErrorMapper.map(e))
                    }
                }
            }
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
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(filePath)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    // Notify MediaScanner so it's removed from Gallery/Music players
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(filePath),
                        null,
                        null
                    )
                }
            }

            if (isVideo) {
                fetchVideoFiles(pathLocation)
            } else {
                fetchMusicFiles(pathLocation)
            }
        }
    }
}
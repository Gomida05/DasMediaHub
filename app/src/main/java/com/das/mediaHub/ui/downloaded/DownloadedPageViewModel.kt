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
import com.das.python.YouTuber.toHumanReadable
import com.das.python.YouTuber.toSimpleDate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DownloadedPageViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    val justExoPlayer: ExoPlayer
) : ViewModel() {

    private val _videoUiState = MutableStateFlow<UiState<List<MediaItem>>>(UiState.Idle)
    val videoUiState = _videoUiState.asStateFlow()

    private val _musicUiState = MutableStateFlow<UiState<List<MediaItem>>>(UiState.Idle)
    val musicUiState = _musicUiState.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private var videoJob: Job? = null
    private var musicJob: Job? = null


    fun initialize(videoPath: String, audioPath: String) {
        videoJob?.cancel()
        musicJob?.cancel()

        videoJob = viewModelScope.launch {
            fetchVideoFiles(videoPath)
        }

        musicJob = viewModelScope.launch {
            fetchMusicFiles(audioPath)
        }
    }


    private suspend fun fetchMusicFiles(pathLocation: String) {
        _musicUiState.value = UiState.Loading

        try {
            val result = loadMediaFiles(
                pathLocation,
                ContentType.MUSIC.extension,
                MediaMetadata.MEDIA_TYPE_MUSIC
            )

            _musicUiState.value =
                if (result.isEmpty()) UiState.Empty
                else UiState.Success(result)

            MediaStoreCache.updateMusicFiles(result)
        } catch (e: Exception) {
            _musicUiState.value = UiState.Error(ErrorMapper.map(e))
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
                    _message.emit("File deleted successfully")
                } else {
                    _message.emit("Failed to delete file")
                }
            } else {
                _message.emit("File does not exist")
            }

            if (isVideo) {
                fetchVideoFiles(pathLocation)
            } else {
                fetchMusicFiles(pathLocation)
            }
        }
    }

    private suspend fun fetchVideoFiles(pathLocation: String) {
        _videoUiState.value = UiState.Loading

        try {
            val result = loadMediaFiles(
                pathLocation,
                ContentType.VIDEO.extension,
                MediaMetadata.MEDIA_TYPE_VIDEO
            )

            _videoUiState.value =
                if (result.isEmpty()) UiState.Empty else UiState.Success(result)

            MediaStoreCache.updateVideosFiles(result)
        } catch (e: Exception) {
            _videoUiState.value = UiState.Error(ErrorMapper.map(e))
        }
    }

    private suspend fun loadMediaFiles(
        pathLocation: String,
        extension: String,
        mediaType: Int
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val dir = File(pathLocation)
        if (!dir.exists() || !dir.isDirectory) return@withContext emptyList()

        dir.listFiles()
            ?.asSequence()
            ?.filter { it.isFile && it.name.endsWith(extension, ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                val lastModified = file.lastModified()
                val formattedDate = lastModified.toSimpleDate()
                val fileSize = file.length().toHumanReadable()

                val metadata = MediaMetadata.Builder()
                    .setTitle(file.nameWithoutExtension)
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
            ?.toList()
            ?: emptyList()
    }
}
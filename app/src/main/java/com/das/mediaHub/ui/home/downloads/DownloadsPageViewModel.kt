package com.das.mediaHub.ui.home.downloads

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DownloadsPageViewModel : ViewModel() {

    private val _videosListData = mutableStateOf<List<MediaItem>>(emptyList())
    val videosListData: State<List<MediaItem>> = _videosListData

    private var _listMusic = mutableStateOf<List<MediaItem>>(emptyList())
    val listMusic: State<List<MediaItem>> = _listMusic
    private val _loading = mutableStateOf(false)
    val isLoading: State<Boolean> = _loading
    private val _error = mutableStateOf<String?>(null)
    val errorFound: State<String?> = _error

    fun fetchVideoFiles(pathLocation: String) {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {

            try{
                val result = loadVideos(pathLocation)
                _videosListData.value = result
            } catch (e: Exception) {
                _error.value = "Something went wrong: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchMusicFiles(pathLocation: String) {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = loadMusics(pathLocation)
                _listMusic.value = result
            } catch (e: Exception) {
                _error.value = "Something went wrong: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun loadVideos(pathLocation: String): MutableList<MediaItem> {
        val downloadedList = mutableListOf<MediaItem>()
        val pathOfFiles = File(pathLocation)
        if (pathOfFiles.exists()) {
            pathOfFiles.listFiles()?.forEach { file ->
                val lastModified = file.lastModified()
                val formattedDate = formatDate(lastModified)
                val fileSizeFormatted = formatFileSize(file.length())
                val mediaMetaData = MediaMetadata.Builder()
                    .setTitle(file.name.removeSuffix(".mp4"))
                    .setDescription(formattedDate)
                    .setArtist(fileSizeFormatted)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_VIDEOS)
                    .build()
                downloadedList.add(
                    MediaItem.Builder()
                        .setMediaId(file.toUri().toString())
                        .setUri(file.toUri())
                        .setMediaMetadata(mediaMetaData)
                        .setTag(fileSizeFormatted)
                        .build()
                )
            }
        }
        return downloadedList
    }

    private fun loadMusics(pathLocation: String): MutableList<MediaItem> {
        val musicMutableList = mutableListOf<MediaItem>()
        val pathOfFiles = File(pathLocation)
        if (pathOfFiles.exists()) {
            pathOfFiles.listFiles()?.forEach { file ->
                val lastModified = file.lastModified()
                val formattedDate = formatDate(lastModified)
                val fileSizeFormatted = formatFileSize(file.length())
                val mediaMetaData = MediaMetadata.Builder()
                    .setTitle(file.name.removeSuffix(".mp3"))
                    .setDescription(formattedDate)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .setArtist(fileSizeFormatted)
                    .build()
                musicMutableList.add(
                    MediaItem.Builder()
                        .setMediaId(file.toUri().toString())
                        .setUri(file.toUri())
                        .setMediaMetadata(mediaMetaData)
                        .setTag(fileSizeFormatted)
                        .build()
                )


            }
        }
        return musicMutableList
    }


    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
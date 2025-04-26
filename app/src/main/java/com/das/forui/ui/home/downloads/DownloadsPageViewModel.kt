package com.das.forui.ui.home.downloads

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DownloadsPageViewModel : ViewModel() {

    private val _videosListData = MutableStateFlow<List<MediaItem>>(emptyList())
    val videosListData: StateFlow<List<MediaItem>> = _videosListData

    fun fetchVideoFiles(pathLocation: String) {
        viewModelScope.launch {
            val downloadedList = mutableListOf<MediaItem>()
            val pathOfFiles = File(pathLocation)
            if (pathOfFiles.exists()) {
                pathOfFiles.listFiles()?.forEach { file ->
                    val lastModified = file.lastModified()
                    val formattedDate = formatDate(lastModified)
                    val fileSizeFormatted = formatFileSize(file.length())
                    val mediaMetaData = MediaMetadata.Builder()
                        .setTitle(file.name.removeSuffix(".mp3"))
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
                _videosListData.value = downloadedList
            }
        }
    }

    private var _listMusic: MutableStateFlow<MutableList<MediaItem>> = MutableStateFlow(mutableListOf())
    val listMusic: StateFlow<MutableList<MediaItem>> = _listMusic

    fun fetchMusicFiles(pathLocation: String) {
        viewModelScope.launch {
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
                _listMusic.value = musicMutableList
            }
        }
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
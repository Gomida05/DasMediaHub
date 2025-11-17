package com.das.mediaHub.ui.players.videoPlayerLocally

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.das.mediaHub.data.local.PathSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class LocalPlayerViewModel(application: Application): AndroidViewModel(application) {

    private val pathSaver by lazy {
        PathSaver(application)
    }
    private val pathLocation = pathSaver.getVideosDownloadPath()
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isError = mutableStateOf<String?>(null)
    val errorFound: State<String?> = _isError
    private val _result = mutableStateOf<List<MediaItem>>(emptyList())

    val mediaItems: State<List<MediaItem>> = _result

    fun loadItems(currentMediaTitle: String) {
        _isLoading.value = true
        _isError.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
               val items = fetchDataFromDB(currentMediaTitle)
                _result.value = items
            } catch (e: Exception) {
                _isError.value = "Found some error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }

    }

    private fun fetchDataFromDB(
        currentMediaTitle: String
    ): List<MediaItem> {

        val pathOfVideos = File(pathLocation)

        if (!pathOfVideos.exists() || !pathOfVideos.isDirectory) return emptyList()

        return pathOfVideos.listFiles()
            ?.asSequence()
            ?.filter { it.isFile && it.name != currentMediaTitle }
            ?.map { file ->
                val metadata = MediaMetadata.Builder()
                    .setTitle(file.name)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                    .build()

                MediaItem.Builder()
                    .setMediaId(file.toUri().toString())
                    .setUri(file.toUri())
                    .setMediaMetadata(metadata)
                    .build()
            }
            ?.toList() ?: emptyList()

    }
}
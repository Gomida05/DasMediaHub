package com.das.mediaHub.ui.players.videoPlayerLocally

import android.app.Application
import android.net.Uri
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
import kotlinx.coroutines.withContext
import java.io.File

class LocalPlayerViewModel(application: Application): AndroidViewModel(application) {

    private val pathLocation = PathSaver(getApplication()).getVideosDownloadPath()
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isError = mutableStateOf<String?>(null)
    val errorFound: State<String?> = _isError
    private val _result = mutableStateOf<List<MediaItem>>(emptyList())

    val mediaItems: State<List<MediaItem>> = _result

    fun loadItems(currentMediaTitle: String) {
        _isLoading.value = true
        _isError.value = null

        viewModelScope.launch {
            try {
               val items = withContext(Dispatchers.IO){
                   fetchDataFromDatabase(currentMediaTitle)
               }
                _result.value = items
            } catch (e: Exception) {
                _isError.value = "Found some error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }

    }

    private fun fetchDataFromDatabase(
        currentMediaTitle: String
    ): MutableList<MediaItem> {

        val fileLists = mutableListOf<MediaItem>().apply {
            clear()
        }
        val pathOfVideos = File(pathLocation)
        if (pathOfVideos.exists()) {
            val fileNames = arrayOfNulls<String>(pathOfVideos.listFiles()?.size ?: 0)
            val pathOfVideosUris = arrayOfNulls<Uri?>(pathOfVideos.listFiles()?.size ?: 0)
            pathOfVideos.listFiles()?.let {
                it.mapIndexed { index, item ->
                    fileNames[index] = item?.name
                    pathOfVideosUris[index] = item?.toUri()

                }
            }
            fileNames.zip(pathOfVideosUris).forEach { (fileName, videoUri) ->
                if (videoUri != null && fileName != null && currentMediaTitle != fileName) {
                    val exoMetadata = MediaMetadata.Builder()
                        .setTitle(fileName)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                        .build()

                    fileLists.add(
                        MediaItem.Builder()
                            .setMediaId(videoUri.toString())
                            .setUri(videoUri)
                            .setMediaMetadata(exoMetadata)
                            .build()
                    )
                }
            }
        }
        return fileLists

    }
}
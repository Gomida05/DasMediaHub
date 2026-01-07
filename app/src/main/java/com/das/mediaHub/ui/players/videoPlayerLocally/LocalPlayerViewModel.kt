package com.das.mediaHub.ui.players.videoPlayerLocally

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LocalPlayerViewModel : ViewModel() {

    private val _mediaItems = mutableStateOf<List<MediaItem>>(emptyList())
    val mediaItems: State<List<MediaItem>> = _mediaItems

    private val _error = mutableStateOf<String?>(null)
    val errorFound: State<String?> = _error

    private var lastScanPath: String? = null
    private var scanJob: Job? = null

    fun loadItemsDebounced(
        currentMediaTitle: String,
        pathLocation: String
    ) {
        if (pathLocation == lastScanPath && _mediaItems.value.isNotEmpty()) return

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            delay(300)
            try {
                val items = withContext(Dispatchers.IO) {
                    scanFolder(currentMediaTitle, pathLocation)
                }
                lastScanPath = pathLocation
                _mediaItems.value = items
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private val cache = mutableMapOf<String, List<MediaItem>>()

    private fun scanFolder(
        currentMediaTitle: String,
        pathLocation: String
    ): List<MediaItem> {

        cache[pathLocation]?.let { return it }

        val dir = File(pathLocation)
        if (!dir.isDirectory) return emptyList()

        val items = dir.listFiles()
            ?.filter { it.isFile && it.name != currentMediaTitle }
            ?.map { file ->
                MediaItem.fromUri(file.toUri())
            }
            ?: emptyList()

        cache[pathLocation] = items
        return items
    }
}

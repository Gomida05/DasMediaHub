package com.das.mediaHub.ui.downloads

import androidx.media3.common.MediaItem

data class DownloadsUiState(
    val videos: List<MediaItem> = emptyList(),
    val musics: List<MediaItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
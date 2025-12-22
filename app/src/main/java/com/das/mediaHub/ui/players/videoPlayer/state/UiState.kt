package com.das.mediaHub.ui.players.videoPlayer.state

data class UiState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
)
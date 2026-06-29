package com.das.mediaHub.data.model.state

import com.das.mediaHub.data.model.interfaces.UiState
import com.das.python.data.model.FewVideoDetails
import com.das.python.data.model.searcher.Video

/**
 * State container for the Video Player UI.
 */
data class VideoPlayerState(
    val videoId: String = "",
    val streamState: UiState<String> = UiState.Idle,
    val detailsState: UiState<FewVideoDetails> = UiState.Idle,
    val suggestionsState: UiState<List<Video>> = UiState.Idle,
    val metadata: VideoUiState = VideoUiState.EMPTY,
    val description: String = ""
)
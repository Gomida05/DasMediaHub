package com.das.mediaHub.ui.players.videoPlayer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.data.model.state.VideoUiState
import com.das.mediaHub.services.download.DownloadService
import com.das.mediaHub.ui.players.videoPlayer.ViewerViewModel
import com.das.mediaHub.ui.players.videoPlayer.components.CustomLayouts.SkeletonSuggestionLoadingLayout
import com.das.mediaHub.ui.players.videoPlayer.components.CustomLayouts.suggestionStateCard
import com.das.python.data.model.searcher.Video


@Composable
fun FullscreenVideoContent(
    modifier: Modifier = Modifier,
    videoState: UiState<String>,
    exoPlayer: ExoPlayer,
    isInFullScreen: Boolean,
    isInPipMode: Boolean,
    onToggleFullscreen: (Boolean) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        VideoScreen(
            videoState = videoState,
            mExoPlayer = exoPlayer,
            isInFullScreen = isInFullScreen,
            isInPipMode = isInPipMode,
            fullScreen = onToggleFullscreen
        )
    }
}



@Composable
fun StandardVideoContent(
    paddings: PaddingValues,
    currentVideoId: String,
    videoState: UiState<String>,
    exoPlayer: ExoPlayer,
    isInFullScreen: Boolean,
    videoUiState: VideoUiState,
    suggestionsState: UiState<List<Video>>,
    viewModel: ViewerViewModel,
    context: android.content.Context,
    onToggleFullscreen: (Boolean) -> Unit,
    onOpenDetailsDialog: () -> Unit,
    onOpenInYoutube: () -> Unit,
    onSelectVideo: (String) -> Unit
) {
    LazyColumn(
        contentPadding = paddings,
        modifier = Modifier.fillMaxSize()
    ) {
        stickyHeader(key = "player_header_$currentVideoId") {
            VideoScreen(
                videoState = videoState,
                mExoPlayer = exoPlayer,
                isInFullScreen = isInFullScreen,
                isInPipMode = false,
                fullScreen = onToggleFullscreen
            )
        }

        if (!isInFullScreen) {
            item(videoUiState.title) {
                VideoDetailsComposable(
                    mContext = context,
                    videoId = currentVideoId,
                    channelThumbnailURL = videoUiState.channelThumbnail ?: "none is here",
                    duration = videoUiState.duration ?: "0:00",
                    viewModel = viewModel,
                    clickForMore = onOpenDetailsDialog,
                    playItInYouTube = onOpenInYoutube
                )
            }

            item {
                Text(
                    text = "Up Next",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            suggestionsContent(
                suggestionsState = suggestionsState,
                videoUiState = videoUiState,
                currentVideoId = currentVideoId,
                viewModel = viewModel,
                context = context,
                onSelectVideo = onSelectVideo
            )
        }
    }
}

fun LazyListScope.suggestionsContent(
    suggestionsState: UiState<List<Video>>,
    videoUiState: VideoUiState,
    currentVideoId: String,
    viewModel: ViewerViewModel,
    context: android.content.Context,
    onSelectVideo: (String) -> Unit
) {
    when (suggestionsState) {
        UiState.Idle -> Unit

        UiState.Loading -> item {
            SkeletonSuggestionLoadingLayout()
        }

        UiState.Empty -> {
            suggestionStateCard(
                icon = Icons.Default.SearchOff,
                title = "No related videos",
                message = "We couldn't find any suggestions for this video."
            )
        }

        is UiState.Error -> {
            suggestionStateCard(
                icon = Icons.Default.WarningAmber,
                title = "Couldn't load suggestions",
                message = suggestionsState.message.ifBlank {
                    "Something went wrong while loading related videos."
                },
                actionLabel = "Try again",
                onAction = {
                    val title = videoUiState.title
                    if (!title.isNullOrEmpty()) {
                        viewModel.fetchSuggestions(
                            videoId = currentVideoId,
                            title = title
                        )
                    }
                },
                isError = true
            )
        }

        is UiState.Success -> {
            items(
                items = suggestionsState.data,
                key = { it.id }
            ) { item ->
                VideoCard(
                    searchItem = item,
                    onPlayThis = { onSelectVideo(item.id) }
                ) { type ->
                    DownloadService.startForYouTube(
                        context = context,
                        id = item.id,
                        title = item.title.orEmpty(),
                        type = type
                    )
                }
            }
        }
    }
}
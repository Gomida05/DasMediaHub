package com.das.mediaHub.ui.players.videoPlayer.components

import android.content.Context
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.Player
import com.das.mediaHub.data.model.interfaces.VideoAction
import com.das.mediaHub.data.model.interfaces.UiState
import com.das.mediaHub.data.model.state.VideoPlayerState
import com.das.mediaHub.data.model.state.VideoUiState
import com.das.mediaHub.ui.components.ErrorStateView
import com.das.mediaHub.ui.players.videoPlayer.components.CustomLayouts.SkeletonSuggestionLoadingLayout
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.openCustomTab
import com.das.python.data.model.searcher.Video


@Composable
fun FullscreenVideoContent(
    modifier: Modifier = Modifier,
    videoState: UiState<String>,
    exoPlayer: Player,
    isInFullScreen: Boolean,
    isInPipMode: Boolean,
    onToggleFullscreen: (Boolean) -> Unit,
    onRetryStreamUrl: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        VideoScreen(
            videoState = videoState,
            player = exoPlayer,
            isInFullScreen = isInFullScreen,
            isInPipMode = isInPipMode,
            fullScreen = onToggleFullscreen,
            onRetry = onRetryStreamUrl
        )
    }
}



@Composable
fun StandardVideoContent(
    paddings: PaddingValues,
    currentVideoId: String,
    isSaved: Boolean,
    uiState: VideoPlayerState,
    player: Player,
    context: Context,
    onToggleFullscreen: (Boolean) -> Unit,
    onFetchSuggestions: (String) -> Unit,
    onSelectVideo: (String) -> Unit,
    onVideoAction: (VideoAction) -> Unit,
    onRetryStreamUrl: () -> Unit
) {
    var expanded by retain { mutableStateOf(false) }



    val windowInfo = LocalWindowInfo.current
    val isWideScreen = windowInfo.containerDpSize.width >= 800.dp

    if (isWideScreen) {
        Row (
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            LazyColumn(
                contentPadding = paddings,
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
            ) {
                item(key = "wide_player_header_$currentVideoId", contentType = "video_player") {
                    VideoScreen(
                        videoState = uiState.streamState,
                        player = player,
                        isInFullScreen = false,
                        isInPipMode = false,
                        fullScreen = onToggleFullscreen,
                        onRetry = onRetryStreamUrl
                    )
                }


                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    VideoDetailsComposable(
                        videoId = currentVideoId,
                        isSaved = isSaved,
                        channelThumbnailURL = uiState.metadata.channelThumbnail ?: "none is here",
                        detailsState = uiState.detailsState,
                        onVideoAction = onVideoAction
                    )
                }

                item {
                    ExpandableVideoDescription(
                        safeDescription = uiState.description,
                        expanded = expanded,
                        openWeb = {
                            context.openCustomTab(it)
                        }
                    ) {
                        expanded = it
                    }

                }
            }


            LazyColumn(
                contentPadding = paddings,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                item {
                    Text(
                        text = "Up Next",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
                    )
                }

                suggestionsContent(
                    suggestionsState = uiState.suggestionsState,
                    videoUiState = uiState.metadata,
                    onSelectVideo = onSelectVideo,
                    onFetchSuggestions = onFetchSuggestions,
                    onVideoAction = onVideoAction
                )
            }
        }
    } else {

        LazyColumn(
            contentPadding = paddings,
            modifier = Modifier.fillMaxSize()
        ) {
            stickyHeader(key = "player_header_$currentVideoId") {
                VideoScreen(
                    videoState = uiState.streamState,
                    player = player,
                    isInFullScreen = false,
                    isInPipMode = false,
                    fullScreen = onToggleFullscreen,
                    onRetry = onRetryStreamUrl
                )
            }

            item(uiState.metadata.title) {
                VideoDetailsComposable(
                    videoId = currentVideoId,
                    isSaved = isSaved,
                    channelThumbnailURL = uiState.metadata.channelThumbnail ?: "none is here",
                    detailsState = uiState.detailsState,
                    onVideoAction = onVideoAction
                )
            }
            item(key = "video_description_item") {
                ExpandableVideoDescription(
                    safeDescription = uiState.description,
                    expanded = expanded,
                    openWeb = {
                        context.openCustomTab(it)
                    }
                ) {
                    expanded = it
                }
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
                suggestionsState = uiState.suggestionsState,
                videoUiState = uiState.metadata,
                onSelectVideo = onSelectVideo,
                onFetchSuggestions = onFetchSuggestions,
                onVideoAction = onVideoAction
            )
        }
    }
}

@Composable
fun ExpandableVideoDescription(
    safeDescription: String,
    expanded: Boolean,
    openWeb: (Uri) -> Unit,
    onClick: (Boolean) -> Unit
) {


    val linkColor = Color(0xFF0000FF)

    val urlPattern = """https?://\S+""".toRegex()

    val hashtagPattern = """(?<=\s|^)#\w+""".toRegex()
    val mentionPattern = """(?<=\s|^)@\w+""".toRegex()

    val annotation = retain (safeDescription) {
        val builder = AnnotatedString.Builder(safeDescription)

        // URLs
        urlPattern.findAll(safeDescription).forEach { match ->
            val url = match.value

            builder.addLink(
                LinkAnnotation.Clickable(
                    tag = url,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    linkInteractionListener = {
                        openWeb(url.toUri())
                    }
                ),
                match.range.first,
                match.range.last + 1
            )
        }

        // Hashtags
        hashtagPattern.findAll(safeDescription).forEach { match ->
            val hashtag = match.value

            builder.addLink(
                LinkAnnotation.Clickable(
                    tag = hashtag,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor
                        )
                    ),
                    linkInteractionListener = {
                        openWeb(
                            "https://www.youtube.com/results?search_query=${hashtag.removePrefix("#")}".toUri()
                        )
                    }
                ),
                match.range.first,
                match.range.last + 1
            )
        }

        // Mentions
        mentionPattern.findAll(safeDescription).forEach { match ->
            val mention = match.value

            builder.addLink(
                LinkAnnotation.Clickable(
                    tag = mention,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor
                        )
                    ),
                    linkInteractionListener = {
                        openWeb(
                            "https://www.youtube.com/${mention.removePrefix("@")}".toUri()
                        )
                    }
                ),
                match.range.first,
                match.range.last + 1
            )
        }

        builder.toAnnotatedString()
    }

    Surface(
        onClick =  {
            if (safeDescription.isNotBlank()) {
                onClick(!expanded)
            }
        },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = annotation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            if (safeDescription.isNotBlank()) {
                Text(
                    text = if (expanded) "Show less" else "Show more",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

fun LazyListScope.suggestionsContent(
    suggestionsState: UiState<List<Video>>,
    videoUiState: VideoUiState,
    onSelectVideo: (String) -> Unit,
    onFetchSuggestions: (String) -> Unit,
    onVideoAction: (VideoAction) -> Unit
) {
    // 1. Section Header: Only show if we have data or are currently loading data
    if (suggestionsState is UiState.Success || suggestionsState is UiState.Loading) {
        item(key = "suggestions_header") {
            Text(
                text = "Up Next",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }

    when (suggestionsState) {
        UiState.Idle -> Unit

        UiState.Loading -> {
            // 2. Loop the skeleton to make it look like an actual list loading
            items(3) {
                SkeletonSuggestionLoadingLayout()
            }
        }

        UiState.Empty -> {
            item(key = "suggestions_empty") {
                ErrorStateView(
                    title = "No related videos",
                    message = "We couldn't find any suggestions for this video.",
                    icon = Icons.Rounded.SearchOff
                )
            }
        }

        is UiState.Error -> {
            item(key = "suggestions_error") {
                // 3. Reused our polished ErrorStateView with native Retry support
                ErrorStateView(
                    title = "Couldn't load suggestions",
                    message = suggestionsState.message.ifBlank {
                        "Something went wrong while loading related videos."
                    },
                    icon = Icons.Rounded.WarningAmber,
                    retryText = "Try again",
                    onRetry = {
                        val title = videoUiState.title
                        if (!title.isNullOrEmpty()) {
                            onFetchSuggestions(title)
                        }
                    }
                )
            }
        }

        is UiState.Success -> {
            items(
                items = suggestionsState.data,
                key = { it.id }
            ) { item ->
                VideoCard(
                    searchItem = item,
                    onPlayThis = { onSelectVideo(item.id) },
                    onVideoAction = onVideoAction
                )
            }
        }
    }
}
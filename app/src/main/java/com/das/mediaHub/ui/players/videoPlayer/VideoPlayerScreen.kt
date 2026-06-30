package com.das.mediaHub.ui.players.videoPlayer

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.das.downloader.data.downloader.DownloadRequest
import com.das.downloader.data.model.download.DownloadType
import com.das.mediaHub.PIP
import com.das.mediaHub.PIP.disablePipAndScreenLock
import com.das.mediaHub.PIP.findActivity
import com.das.mediaHub.PIP.rememberIsInPipMode
import com.das.mediaHub.PIP.rememberPipModifier
import com.das.mediaHub.data.model.interfaces.VideoAction
import com.das.mediaHub.data.model.interfaces.UiState
import com.das.mediaHub.services.download.DownloadDispatcher
import com.das.mediaHub.services.media.online.OnlineBackgroundPlayer.Companion.playAudioFromUrl
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.openCustomTab
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.rotateScreen
import com.das.mediaHub.ui.players.videoPlayer.components.FullscreenVideoContent
import com.das.mediaHub.ui.players.videoPlayer.components.StandardVideoContent
import com.das.mediaHub.ui.players.videoPlayer.components.VideoPlayerEffects


@Composable
fun VideoPlayerScreen(
    videoID: String,
    onNavigateUp: () -> Unit
) {

    val viewModel = hiltViewModel<VideoPlayerViewModel>()

    val context = LocalContext.current
    val activity = context.findActivity()

    var currentVideoId by rememberSaveable { mutableStateOf(videoID) }
    var isInFullScreen by rememberSaveable { mutableStateOf(false) }

    val isInPipMode = rememberIsInPipMode()



    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()

    val streamUrl = (uiState.streamState as? UiState.Success<String>)?.data


    val player = viewModel.player

    var dialogState by retain { mutableStateOf<ActionDialogState>(ActionDialogState.Idle) }

    // Logic for initializing and playing
    VideoPlayerEffects(
        window = activity?.window,
        videoId = currentVideoId,
        videoUrl = streamUrl,
        player = player,
        onInitialize = viewModel::loadVideo,
        onPlayNext = viewModel::playVideo,
        onDisposePlayer = {
            disablePipAndScreenLock(activity)
            viewModel.closeCurrentlyPlayingMedia()
        }
    )

    BackHandler {
        if (isInFullScreen) {
            isInFullScreen = false
            activity?.rotateScreen(false)
        } else {
            onNavigateUp()
        }
    }


    PIP.BindPip(activity = activity)
    PIP.HandlePip(activity = activity)

    val onToggleFullscreen: (Boolean) -> Unit = { enabled ->
        isInFullScreen = enabled
        activity?.rotateScreen(enabled)
    }


    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
    ) { paddings ->
        if (isInFullScreen || isInPipMode) {
            FullscreenVideoContent(
                modifier = Modifier
                    .fillMaxSize()
                    .rememberPipModifier(),
                videoState = uiState.streamState,
                exoPlayer = player,
                isInFullScreen = isInFullScreen,
                isInPipMode = isInPipMode,
                onToggleFullscreen = onToggleFullscreen,
                onRetryStreamUrl = {
                    viewModel.fetchStreamUrl(videoId = videoID)
                }
            )
        } else {
            StandardVideoContent(
                paddings = paddings,
                currentVideoId = currentVideoId,
                isSaved = isSaved,
                uiState = uiState,
                player = player,
                context = context,
                onToggleFullscreen = onToggleFullscreen,
                onFetchSuggestions = { videoTitle->
                    viewModel.fetchSuggestions(
                        videoId = currentVideoId,
                        title = videoTitle
                    )
                },
                onSelectVideo =  { selectedId -> currentVideoId = selectedId },
                onVideoAction = { action ->
                    context.handleAction(videoId = videoID, action = action,
                        dialogState = {
                            dialogState = it
                        }, viewModel = viewModel)
                },
                onRetryStreamUrl = {
                    viewModel.fetchStreamUrl(videoId = videoID)
                }
            )

            ActionStatusDialog(dialogState) {
                dialogState = ActionDialogState.Idle
            }
        }
    }
}


@Composable
fun ActionStatusDialog(
    state: ActionDialogState,
    onDismiss: () -> Unit
) {
    when (state) {
        ActionDialogState.Loading -> {
            Dialog(onDismissRequest = {}) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Starting background play...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please wait a moment",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

        }

        is ActionDialogState.Error -> {


            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(28.dp),
                title = {
                    Text(
                        text = "Couldn't play video",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = { Text(state.message) },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            )
        }

        ActionDialogState.Idle -> {
            onDismiss()
        }
    }
}

sealed interface ActionDialogState {
    object Idle : ActionDialogState
    object Loading : ActionDialogState
    data class Error(val message: String) : ActionDialogState
}

fun Context.handleAction(
    videoId: String,
    action: VideoAction,
    dialogState: (ActionDialogState) -> Unit,
    viewModel: VideoPlayerViewModel
) {
    when (action) {
        VideoAction.ToggleHistory -> {
            viewModel.addHistory()
        }
        is VideoAction.ToggleFavorite -> {
            if (!action.insert) {
                viewModel.deleteFromFavDb()
            } else {
                viewModel.addToFavDb()
            }
        }

        VideoAction.PlayInYoutube -> {
            viewModel.pause()
            openInYoutube(
                videoId = videoId,
                currentPosition = viewModel.currentPosition,
            )
        }
        VideoAction.PlayBackground -> {
            viewModel.loadStreamForBackGroud(
                onStart = {
                    dialogState(ActionDialogState.Loading)
                },
                onSuccess = { streamResult ->
                    if (streamResult.audioUrl.isBlank()) {
                        dialogState(
                            ActionDialogState.Error(
                                "This video can’t be played in the background right now."
                            )
                        )
                        return@loadStreamForBackGroud
                    }

                    dialogState(ActionDialogState.Idle)

                    playAudioFromUrl(
                        audioUrl = streamResult.audioUrl,
                        selectedItem = streamResult
                    )
                },
                onFailure = {
                    dialogState(
                        ActionDialogState.Error(message = it)
                    )
                }
            )
        }
        VideoAction.Share -> {
            shareVideo(
                context = this,
                videoId = videoId
            )
        }
        is VideoAction.Download -> {
            when (action.type) {
                DownloadType.YOUTUBE_VIDEO -> {
                    DownloadDispatcher.enqueue(
                        context = this,
                        request = DownloadRequest.YoutubeVideo(
                            videoId = videoId,
                            title = action.title
                        )
                    )
                }
                DownloadType.YOUTUBE_AUDIO -> {
                    DownloadDispatcher.enqueue(
                        context = this,
                        request = DownloadRequest.YoutubeAudio(
                            videoId = videoId,
                            title = action.title
                        )
                    )
                }
                else -> Unit
            }
        }

    }
}

fun Context.openInYoutube(
    videoId: String,
    currentPosition: Long
)  {
    val currentTimeSec = currentPosition / 1000
    val youtubeUrl = "https://www.youtube.com/watch?v=$videoId&t=${currentTimeSec}s".toUri()
    try {

        val intent = Intent(Intent.ACTION_VIEW, youtubeUrl).apply {
            setPackage("com.google.android.youtube")
        }
        startActivity(intent)
    } catch (_: Exception) {
        openCustomTab(youtubeUrl)
    }
}

private fun shareVideo(
    context: Context,
    videoId: String
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "https://youtu.be/$videoId"
        )
    }

    context.startActivity(
        Intent.createChooser(intent, "Share via")
    )
}
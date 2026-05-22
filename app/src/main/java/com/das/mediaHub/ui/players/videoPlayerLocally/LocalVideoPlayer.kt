package com.das.mediaHub.ui.players.videoPlayerLocally

import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import com.das.mediaHub.PIP
import com.das.mediaHub.PIP.HandlePip
import com.das.mediaHub.PIP.findActivity
import com.das.mediaHub.data.mediacontroller.online.VideoPlayerListener
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.rotateScreen


@Composable
fun LocalVideoPlayer(videoUri: String) {
    val context = LocalContext.current
    val activity = context.findActivity()

    PIP.BindPip(activity = activity)
    HandlePip(activity = activity)

    val viewModel = hiltViewModel<LocalPlayerViewModel>()

    val metadataState by viewModel.currentMediaMetadata.collectAsStateWithLifecycle()
    val playlistState by viewModel.uiState.collectAsStateWithLifecycle()

    var controller by retain { mutableStateOf<MediaController?>(null) }
    val snackBarHostState = remember { SnackbarHostState() }

    val fallbackMetadata = retain (videoUri) {
        MediaMetadata.Builder()
            .setTitle(videoUri.substringAfterLast("/"))
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .build()
    }

    val resolvedMetadata = when (val state = metadataState) {
        is UiState.Success -> state.data
        else -> fallbackMetadata
    }

    val currentItem = remember(videoUri, resolvedMetadata) {
        MediaItem.Builder()
            .setMediaId(videoUri)
            .setUri(videoUri.toUri())
            .setMediaMetadata(resolvedMetadata)
            .build()
    }

    LaunchedEffect(videoUri) {
        viewModel.init(videoUri = videoUri)
    }

    LaunchedEffect(Unit) {
        PlayerControllerHolder.getOrCreate(context) { readyController ->
            controller = readyController


            if (readyController.currentMediaItem?.mediaId != currentItem.mediaId) {
                readyController.setMediaItem(currentItem)
                readyController.prepare()
                readyController.play()
            } else {
                readyController.play()
            }
            readyController.addListener(
                VideoPlayerListener {
                    readyController.seekToNext()
                }
            )
        }
    }

    DisposableEffect(Unit) {
        activity?.let {
            it.rotateScreen(fullScreen = true)
            it.window.setFlags(FLAG_KEEP_SCREEN_ON, FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            PIP.disablePipAndScreenLock(activity = activity)
            PlayerControllerHolder.release(context.applicationContext)
        }
    }

    LaunchedEffect(controller, currentItem) {
        val mediaController = controller ?: return@LaunchedEffect

        if (mediaController.currentMediaItem?.mediaId != currentItem.mediaId) {
            mediaController.setMediaItem(currentItem)
            mediaController.prepare()
            mediaController.play()
        } else {
            mediaController.play()
        }
    }

    LaunchedEffect(playlistState, controller, currentItem) {
        val mediaController = controller ?: return@LaunchedEffect
        val successState = playlistState as? UiState.Success<List<MediaItem>> ?: return@LaunchedEffect

        val playlist = buildList {
            add(currentItem)
            addAll(successState.data.filterNot { it.mediaId == currentItem.mediaId })
        }

        if (playlist.isEmpty()) return@LaunchedEffect

        val alreadySameQueue =
            mediaController.mediaItemCount == playlist.size &&
                    playlist.indices.all { index ->
                        mediaController.getMediaItemAt(index).mediaId == playlist[index].mediaId
                    }

        if (!alreadySameQueue) {
            mediaController.setMediaItems(playlist, 0, 0L)
            mediaController.prepare()
            mediaController.play()
        }
    }

    LaunchedEffect(playlistState, controller) {
        if (controller == null) return@LaunchedEffect

        when (val state = playlistState) {
            is UiState.Empty -> {
                snackBarHostState.showSnackbar(
                    message = "No more videos found in this folder",
                    duration = SnackbarDuration.Short
                )
            }

            is UiState.Error -> {
                snackBarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
            }

            else -> Unit
        }
    }

    val metadataError = metadataState as? UiState.Error
    val showFullScreenLoading = controller == null
    val showFullScreenError = controller == null && metadataError != null
    val showPlaylistLoadingOverlay = controller != null && playlistState is UiState.Loading
    val isInPipMode = PIP.rememberIsInPipMode()


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            controller?.let {
                CustomPlayer(
                    player = it,
                    isFullScreen = null,
                    onFullScreenChanged = { isFullScreen ->
                        activity?.rotateScreen(fullScreen = isFullScreen)
                    },
                    showControls = !isInPipMode
                )
            }

            when {
                showFullScreenLoading -> {
                    PlayerStateView(
                        title = "Loading video",
                        message = "Preparing your player..."
                    )
                }

                showFullScreenError -> {
                    PlayerStateView(
                        title = "Couldn’t open video",
                        message = metadataError.message,
                        showProgress = false
                    )
                }
            }

            if (showPlaylistLoadingOverlay) {
                LoadingHint()
            }
        }
    }
}

@Composable
private fun PlayerStateView(
    title: String,
    message: String,
    showProgress: Boolean = true
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showProgress) {
                    CircularProgressIndicator()
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LoadingHint() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            tonalElevation = 4.dp
        ) {
            Text(
                text = "Scanning folder videos...",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
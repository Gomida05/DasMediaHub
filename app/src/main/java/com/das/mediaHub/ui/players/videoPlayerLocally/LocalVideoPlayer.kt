package com.das.mediaHub.ui.players.videoPlayerLocally

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import com.das.mediaHub.ui.players.videoPlayer.CustomMethods.rotateScreen
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import java.io.File

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun LocalVideoPlayer(videoUri: String) {
    val context = LocalContext.current
    val activity = LocalActivity.current as ComponentActivity

    val viewModel = viewModel(
        modelClass = LocalPlayerViewModel::class.java.kotlin,
        factory = viewModelFactory {
            initializer {
                LocalPlayerViewModel(context.contentResolver)
            }
        }
    )

    val metadataState by viewModel.currentMediaMetadata.collectAsStateWithLifecycle()
    val playlistState by viewModel.uiState.collectAsStateWithLifecycle()

    var controller by remember { mutableStateOf<MediaController?>(null) }

    val isContentUri = remember(videoUri) {
        videoUri.startsWith("content://")
    }

    val folderPath = remember(videoUri, isContentUri) {
        if (isContentUri) "" else File(videoUri).parent.orEmpty()
    }

    val fallbackMetadata = remember(videoUri) {
        MediaMetadata.Builder()
            .setTitle(videoUri.substringAfterLast("/"))
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .build()
    }

    val resolvedMetadata = when (val state = metadataState) {
        is UiState.Success -> state.data
        is UiState.Error -> fallbackMetadata
        UiState.Empty, UiState.Idle, UiState.Loading -> fallbackMetadata
    }

    val currentItem = remember(videoUri, resolvedMetadata) {
        MediaItem.Builder()
            .setMediaId(videoUri)
            .setUri(videoUri.toUri())
            .setMediaMetadata(resolvedMetadata)
            .build()
    }

    LaunchedEffect(Unit) {
        PlayerControllerHolder.getOrCreate(context) { readyController ->
            controller = readyController
        }
    }

    LaunchedEffect(videoUri) {
        activity.rotateScreen(fullScreen = true)
        viewModel.loadCurrentMediaInfo(videoUri.toUri())
    }

    // Play current item as soon as controller exists.
    LaunchedEffect(controller, currentItem) {
        val mediaController = controller ?: return@LaunchedEffect

        if (mediaController.currentMediaItem?.mediaMetadata != currentItem.mediaMetadata) {
            mediaController.setMediaItem(currentItem)
            mediaController.prepare()
            mediaController.play()
        } else {
            mediaController.play()
        }
    }

    // Load folder playlist only for file paths.
    LaunchedEffect(folderPath, isContentUri, currentItem) {
        if (!isContentUri && folderPath.isNotBlank()) {
            viewModel.loadItemsDebounced(
                currentMediaTitle = currentItem.mediaMetadata.title?.toString().orEmpty(),
                pathLocation = folderPath
            )
        }
    }

    // Upgrade from single item to playlist after folder scan succeeds.
    LaunchedEffect(playlistState, controller, currentItem, isContentUri) {
        if (isContentUri) return@LaunchedEffect

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

    RetainedEffect(Unit) {
        onRetire {
            activity.rotateScreen(fullScreen = false)
        }
    }

    val showMetadataLoading = metadataState is UiState.Loading && controller == null
    val metadataError = metadataState as? UiState.Error
    val playlistError = playlistState as? UiState.Error

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            CustomPlayer(
                controller
            )
/**            AndroidView(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.Black)
                    .zIndex(0f),
                factory = {
                    PlayerView(context).apply {
                        keepScreenOn = true
                        useController = true
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        player = controller
                    }
                },
                update = { playerView ->
                    playerView.player = controller
                }
            )*/
            if (controller != null && controller?.isPlaying == true) {
                when {
                    showMetadataLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    metadataError != null && controller == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = metadataError.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    playlistState is UiState.Loading && !isContentUri -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    playlistState is UiState.Empty && !isContentUri -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No videos found",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    playlistError != null && !isContentUri -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = playlistError.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
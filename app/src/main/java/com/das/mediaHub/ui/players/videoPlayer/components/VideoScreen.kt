package com.das.mediaHub.ui.players.videoPlayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.das.mediaHub.PIP.rememberPipModifier
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.ui.players.videoPlayerLocally.CustomPlayer


@Composable
fun VideoScreen(
    videoState: UiState<String>,
    mExoPlayer: ExoPlayer,
    isInFullScreen: Boolean,
    isInPipMode: Boolean,
    fullScreen: (Boolean) -> Unit
) {
    val playerModifier = when {
        isInPipMode -> Modifier.rememberPipModifier()
        isInFullScreen -> Modifier.fillMaxSize()
        else -> Modifier
            .fillMaxWidth()
            .height(230.dp)
    }

    Box(
        modifier = playerModifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        CustomPlayer(
            player = mExoPlayer,
            showControls = !isInPipMode,
            isFullScreen = isInFullScreen,
            onFullScreenChanged = fullScreen,
            modifier = Modifier.fillMaxSize()
        )

        // 🔥 Always keep PlayerView alive

        /**
         *         AndroidView(
         *             modifier = Modifier.fillMaxSize(),
         *             factory = {
         *                 PlayerView(it).apply {
         *                     player = mExoPlayer
         *                     keepScreenOn = true
         *                 }
         *             },
         *             update = { playerView ->
         *                 playerView.player = mExoPlayer
         *                 playerView.setFullscreenButtonState(isInFullScreen)
         *                 playerView.setFullscreenButtonClickListener { fullScreen(it) }
         *                 playerView.resizeMode =
         *                     if (isInFullScreen)
         *                         AspectRatioFrameLayout.RESIZE_MODE_ZOOM
         *                     else
         *                         AspectRatioFrameLayout.RESIZE_MODE_FIT
         *
         *                 playerView.useController = !isInPipMode
         *             }
         *         )
         */

        // 🔥 Overlay states (instead of replacing player)
        when (videoState) {
            UiState.Idle,
            UiState.Loading -> {
                // Only show spinner if nothing is loaded yet
                if (mExoPlayer.currentMediaItem == null) {
                    CircularProgressIndicator()
                }
            }

            UiState.Empty -> {
                Text(
                    text = "No video found",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            is UiState.Error -> {
                if (!isInPipMode) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = videoState.message,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is UiState.Success -> Unit
        }
    }
}
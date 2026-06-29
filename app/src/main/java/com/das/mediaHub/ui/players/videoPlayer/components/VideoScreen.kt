package com.das.mediaHub.ui.players.videoPlayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.das.mediaHub.PIP.rememberPipModifier
import com.das.mediaHub.data.model.interfaces.UiState
import com.das.mediaHub.ui.players.videoPlayerLocally.CustomPlayer


@Composable
fun VideoScreen(
    videoState: UiState<String>,
    player: Player,
    isInFullScreen: Boolean,
    isInPipMode: Boolean,
    fullScreen: (Boolean) -> Unit,
    onRetry: () -> Unit
) {
    val playerModifier = when {
        isInPipMode -> Modifier.rememberPipModifier()
        isInFullScreen -> Modifier.fillMaxSize()
        else -> Modifier
            .fillMaxWidth()
            .height(230.dp)
    }

    Box(
        modifier = playerModifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        CustomPlayer(
            player = player,
            showControls = !isInPipMode,
            isFullScreen = isInFullScreen,
            onFullScreenChanged = fullScreen,
            modifier = Modifier.fillMaxSize()
        )

        when (videoState) {
            UiState.Idle,
            UiState.Loading -> {
                if (player.currentMediaItem == null && !isInPipMode) {
                    VideoLoadingOverlay()
                }
            }

            UiState.Empty -> {
                if (!isInPipMode) {
                    VideoEmptyOverlay()
                }
            }

            is UiState.Error -> {
                if (!isInPipMode) {
                    VideoErrorOverlay(message = videoState.message, onRetry = onRetry)
                }
            }

            is UiState.Success -> Unit
        }
    }
}


/**
 * A sleek, semi-transparent error overlay.
 * Dims the background to ensure the error is legible even if a bright video frame is stuck underneath.
 */
@Composable
private fun VideoErrorOverlay(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(
                horizontal = 32.dp,
                vertical = 16.dp
            )
        ) {

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = "Playback Error",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Playback Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )


            Spacer(modifier = Modifier.height(20.dp))

            FilledTonalButton(
                onClick = onRetry,
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Retry")
            }
        }
    }
}

/**
 * Empty state overlay, styled similarly to the error overlay but using neutral colors.
 */
@Composable
private fun VideoEmptyOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.VideocamOff,
                contentDescription = "No Video Found",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No video available",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

/**
 * Styled loading spinner designed specifically for dark media player environments.
 */
@Composable
private fun VideoLoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)), // Lighter dimming for loading
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.White.copy(alpha = 0.2f),
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
    }
}


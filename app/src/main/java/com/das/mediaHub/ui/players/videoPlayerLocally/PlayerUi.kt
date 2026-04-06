package com.das.mediaHub.ui.players.videoPlayerLocally

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.ui.compose.state.NextButtonState
import androidx.media3.ui.compose.state.PlayPauseButtonState
import androidx.media3.ui.compose.state.PreviousButtonState
import java.util.Locale


@SuppressLint("UnsafeOptInUsageError")
@Composable
fun PlayerUi(
    playPauseButtonState: PlayPauseButtonState,
    currentPosition: Long,
    duration: Long,
    isBuffering: Boolean,
    onSeekPositionChange: (Long) -> Unit,
    onSeekPositionChangeFinished: (Long) -> Unit,
    nextButtonState: NextButtonState,
    previousButtonState: PreviousButtonState

) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color.Black
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = previousButtonState::onClick,
                enabled = previousButtonState.isEnabled,
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Rewind",
                    tint = if (previousButtonState.isEnabled) Color.White else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }
            if (isBuffering) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                IconButton(
                    onClick = playPauseButtonState::onClick,
                    enabled = playPauseButtonState.isEnabled,
                    modifier = Modifier.size(100.dp)
                ) {
                    Icon(
                        imageVector = if (playPauseButtonState.showPlay) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (playPauseButtonState.showPlay) "Play" else "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            IconButton(
                onClick = nextButtonState::onClick,
                enabled = nextButtonState.isEnabled,
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = if (nextButtonState.isEnabled) Color.White else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Row (
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                currentPosition.formatToDuration(),
                color = Color.White
            )

            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { onSeekPositionChange(it.toLong()) },
                onValueChangeFinished = {
                    onSeekPositionChangeFinished(currentPosition)
                },
                valueRange = 0f..duration.toFloat(),
                track = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(
                                    it.value / duration
                                )
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                },
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .shadow(elevation = 4.dp, CircleShape)
                            .background(Color.White)
                    )
                },
                modifier = Modifier.weight(1f)

            )
            Text(
                duration.formatToDuration(),
                color = Color.White
            )
        }
    }
}

private fun Long.formatToDuration(): String {
    val totalSec = this / 1000
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60

    return if (hours > 0) {
        String.format(Locale.UK, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.UK, "%02d:%02d", minutes, seconds)
    }
}
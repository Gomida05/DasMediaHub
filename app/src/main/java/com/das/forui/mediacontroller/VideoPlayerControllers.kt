package com.das.forui.mediacontroller

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import java.util.Locale
import java.util.concurrent.TimeUnit


@SuppressLint("UnsafeOptInUsageError")
object VideoPlayerControllers {


    @Composable
    fun PlayPauseButton(player: Player, modifier: Modifier = Modifier) {
        val state = rememberPlayPauseButtonState(player)

        IconButton(onClick = state::onClick, modifier = modifier, enabled = state.isEnabled) {
            Icon(
                imageVector = if (state.showPlay) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (state.showPlay) "play_button"
                else "pause_button"
            )
        }
    }


    @Composable
    fun PreviousButton(player: Player, modifier: Modifier = Modifier) {
        val state = rememberPreviousButtonState(player)

        IconButton(onClick = state::onClick, modifier = modifier, enabled = state.isEnabled) {

            Icon(
                imageVector = Icons.Default.SkipPrevious,
                ""
            )
        }

    }

    @Composable
    fun NextButton(player: Player, modifier: Modifier = Modifier) {
        val state = rememberNextButtonState(player)

        IconButton(onClick = state::onClick, modifier = modifier, enabled = state.isEnabled) {

            Icon(
                imageVector = Icons.Default.SkipNext,
                ""
            )
        }

    }

    @Composable
    fun BottomControls(
        modifier: Modifier = Modifier,
        totalDuration: () -> Long = { 100L },
        currentTime: () -> Long = {20L},
        bufferPercentage: () -> Int = {2},
        onSeekChanged: (timeMs: Float) -> Unit = { time ->
            println(time)
        }
    ) {

        val duration = remember(totalDuration()) { totalDuration() }

        val videoTime = remember(currentTime()) { currentTime() }

        val buffer = remember(bufferPercentage()) { bufferPercentage() }

        Column(modifier = modifier.padding(bottom = 12.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // buffer bar
                Slider(
                    value = buffer.toFloat(),
                    enabled = false,
                    onValueChange = { },
                    valueRange = 0f..100f,
                    colors =
                    SliderDefaults.colors(
                        disabledThumbColor = Color.Transparent,
                        disabledActiveTrackColor = Color.Gray
                    )
                )

                // seek bar
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = videoTime.toFloat(),
                    onValueChange = onSeekChanged,
                    valueRange = 0f..duration.toFloat(),
                    colors =
                    SliderDefaults.colors(
                        thumbColor = Color(0xFFBB86FC),
                        activeTickColor = Color(0xFFBB86FC)
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = duration.formatMinSec(),
                    color = Color(0xFFBB86FC)
                )

                IconButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = {}
                ) {
                    Image(
                        contentScale = ContentScale.Crop,
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Enter/Exit fullscreen"
                    )
                }
            }
        }
    }


    fun Long.formatMinSec(): String {
        return if (this == 0L) {
            "..."
        } else {
            String.format(
                Locale.ENGLISH,
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(this),
                TimeUnit.MILLISECONDS.toSeconds(this) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(this)
                        )
            )
        }
    }
}
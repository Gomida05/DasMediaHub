package com.das.forui.mediacontroller

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import kotlinx.coroutines.delay
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
    fun TopControls(navigateUp: ()-> Unit){




        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            IconButton(
                onClick = navigateUp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    "navigateUp Icon"
                )
            }

            IconButton(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    "settings Icon"
                )
            }


        }
    }

    @Composable
    fun BottomControls(
        player: ExoPlayer,
        modifier: Modifier = Modifier,
        onFullScreen: ()-> Unit
    ) {
        // Reactive state for duration, current time, and buffer
        var currentPosition by remember { mutableLongStateOf(0L) }
        var totalDuration by remember { mutableLongStateOf(0L) }
        var bufferedPercent by remember { mutableIntStateOf(0) }


        LaunchedEffect(player) {
            while (true) {
                currentPosition = player.currentPosition
                totalDuration = player.duration.takeIf { it > 0 } ?: 0L
                bufferedPercent = player.bufferedPercentage
                delay(500L) // Update every half second
            }
        }

        Column(modifier = modifier) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Buffer bar
                Slider(
                    value = bufferedPercent.toFloat().coerceIn(0f, 100f),
                    onValueChange = {},
                    enabled = false,
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        disabledThumbColor = Color.Transparent,
                        disabledActiveTrackColor = Color.Gray
                    )
                )

                if (totalDuration > 0) {
                    Slider(
                        modifier = Modifier.fillMaxWidth(),
                        value = currentPosition.coerceIn(0, totalDuration).toFloat(),
                        onValueChange = {
                            player.seekTo(it.toLong())
                        },
                        valueRange = 0f..totalDuration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFBB86FC),
                            activeTickColor = Color(0xFFBB86FC)
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .height(22.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = totalDuration.formatMinSec(),
                    color = Color(0xFFBB86FC),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                ) {

                    Text(
                        text = currentPosition.formatMinSec(),
                        color = Color(0xFFBB86FC),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                    IconButton(
                        onClick = onFullScreen
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Enter/Exit fullscreen"
                        )
                    }
                }
            }
        }

    }



    private fun Long.formatMinSec(): String {
        return if (this == 0L) {
            "..."
        } else {
            String.format(
                Locale.ENGLISH,
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(this),
                TimeUnit.MILLISECONDS.toSeconds(this) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
            )
        }
    }
}


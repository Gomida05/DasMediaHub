package com.das.forui.mediacontroller

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.zIndex
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import com.das.forui.objectsAndData.Youtuber.formatTimeFromMs
import kotlinx.coroutines.delay


@SuppressLint("UnsafeOptInUsageError")
object VideoPlayerControllers {



    @Composable
    fun PlayerControls(
        mExoPlayer: ExoPlayer,
        modifier: Modifier = Modifier,
        isVisible: () -> Boolean,
        navigateUp: () -> Unit
    ) {



        AnimatedVisibility(
            modifier = modifier
                .fillMaxSize(),
            visible = isVisible(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
            ) {

                TopControls(navigateUp = navigateUp)
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp)
                ) {
                    PreviousButton(mExoPlayer)
                    PlayPauseButton(mExoPlayer)
                    NextButton(mExoPlayer)

                }

                BottomControls(player = mExoPlayer, onFullScreen = {  })
            }
        }
    }
    @Composable
    fun PlayPauseButton(player: Player, modifier: Modifier = Modifier) {
        val state = rememberPlayPauseButtonState(player)

        IconButton(onClick = state::onClick, modifier = modifier, enabled = state.isEnabled) {
            Icon(
                imageVector = if (state.showPlay) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (state.showPlay) "play_button"
                else "pause_button",
                tint = Color(0xFFBB86FC)
            )
        }
    }


    @Composable
    fun PreviousButton(player: Player, modifier: Modifier = Modifier) {
        val state = rememberPreviousButtonState(player)

        IconButton(onClick = state::onClick, modifier = modifier, enabled = state.isEnabled) {

            Icon(
                imageVector = Icons.Default.SkipPrevious,
                "",
                tint = Color(0xFFBB86FC)
            )
        }

    }

    @Composable
    fun NextButton(player: Player, modifier: Modifier = Modifier) {
        val state = rememberNextButtonState(player)

        IconButton(onClick = state::onClick, modifier = modifier, enabled = state.isEnabled) {

            Icon(
                imageVector = Icons.Default.SkipNext,
                "",
                tint = Color(0xFFBB86FC)
            )
        }

    }



    @Composable
    fun TopControls(
        navigateUp: ()-> Unit
    ){

        Box(
            modifier = Modifier
                .fillMaxWidth()
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp), // give some height to avoid overlap issues
                contentAlignment = Alignment.Center
            ) {
                // Buffered progress (non-interactive)
                Slider(
                    value = bufferedPercent.toFloat().coerceIn(0f, 100f),
                    onValueChange = {},
                    enabled = false,
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        disabledThumbColor = Color.Transparent,
                        disabledActiveTrackColor = Color.Gray,
                        disabledInactiveTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(0f) // put it behind
                )

                // Playback progress (interactive)
                if (totalDuration > 0) {
                    Slider(
                        value = currentPosition.coerceIn(0, totalDuration).toFloat(),
                        onValueChange = {
                            player.seekTo(it.toLong())
                        },
                        valueRange = 0f..totalDuration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFBB86FC),
                            activeTrackColor = Color(0xFFBB86FC),
                            inactiveTrackColor = Color.LightGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f) // put it above
                    )
                }
            }

            Box(
                modifier = Modifier
                    .height(22.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = currentPosition.formatTimeFromMs(),
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
                        text = totalDuration.formatTimeFromMs(),
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



}


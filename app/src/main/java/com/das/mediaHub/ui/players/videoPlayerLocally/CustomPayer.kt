package com.das.mediaHub.ui.players.videoPlayerLocally

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.indicators.ProgressIndicator
import androidx.media3.ui.compose.material3.Player
import androidx.media3.ui.compose.material3.buttons.PlaybackSpeedToggleButton
import androidx.media3.ui.compose.material3.buttons.RepeatButton
import androidx.media3.ui.compose.material3.buttons.ShuffleButton
import androidx.media3.ui.compose.material3.indicator.PositionAndDurationText
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CustomPlayer(
    player: MediaController
) {

    var isPlayerUiVisible by retain { mutableStateOf(true) }

    var isSeeking by retain { mutableStateOf(false) }
    var isPlaying by retain { mutableStateOf(false) }
    var isBuffering by retain { mutableStateOf(false) }
    var currentPosition by retain { mutableLongStateOf(0L) }
    var duration by retain { mutableLongStateOf(0L) }


    val playPauseButtonState = rememberPlayPauseButtonState(player)
    val previousButtonState = rememberPreviousButtonState(player)
    val nextButtonState = rememberNextButtonState(player)


    RetainedEffect(player) {
        val listener = object : Player.Listener {

            override fun onIsPlayingChanged(playing: Boolean) {
                super.onIsPlayingChanged(playing)
                 isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    duration = player.duration.coerceAtLeast(0)
                }

            }
        }

        player.addListener(listener)
        onRetire {
            player.removeListener(listener)
        }
    }

    LaunchedEffect(isPlayerUiVisible, isSeeking, isPlaying) {
        delay(5000L.milliseconds)
        if (!isSeeking) {
            isPlayerUiVisible = false
        }
    }

    LaunchedEffect(player, isPlaying, isSeeking) {
        while (isPlaying) {
            if (!isSeeking) {
                currentPosition = player.currentPosition.coerceAtLeast(0)
            }
            delay(16L.milliseconds)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            ContentFrame(
                player = player,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = null
                    ) {
                        isPlayerUiVisible = !isPlayerUiVisible
                    }
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                AnimatedVisibility(
                    visible = isPlayerUiVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    PlayerUi(
                        playPauseButtonState = playPauseButtonState,
                        currentPosition = currentPosition,
                        duration = duration,
                        isBuffering = isBuffering,
                        onSeekPositionChange = {
                            isSeeking = true
                            currentPosition = it
                        },
                        onSeekPositionChangeFinished = {
                            isSeeking = false
                            player.seekTo(it)
                        },
                        previousButtonState = previousButtonState,
                        nextButtonState = nextButtonState
                    )
                }
            }
        }
    }
}

@Composable
fun CustomPlayer(
    player: Player,
    isFullScreen: Boolean?,
    onFullScreenChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showControls: Boolean = true
) {
    var isPlayerUiVisible by retain { mutableStateOf(showControls) }
    var isSeeking by retain { mutableStateOf(false) }

    LaunchedEffect(isPlayerUiVisible) {
        if (!showControls) return@LaunchedEffect
        delay(5000L.milliseconds)
        if (!isSeeking) {
            isPlayerUiVisible = false
        }
    }

    Player(
        player = player,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = null
            ) {
                isPlayerUiVisible = !isPlayerUiVisible
            },
        keepContentOnReset = true,
        showControls = isPlayerUiVisible,
        bottomControls = { player, showControls ->
            BottomControls(
                showControls = showControls,
                player = player,
                isFullScreen = isFullScreen,
                isSeeking = {
                    isSeeking = it
                },
            ) {
                onFullScreenChanged(it)
            }
        }
    )
}


@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun BottomControls(
    showControls: Boolean,
    player: Player?,
    isFullScreen: Boolean?,
    isSeeking: (Boolean) -> Unit,
    onFullScreenChanged: (Boolean) -> Unit,
) {
    AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 15.dp)) {
            MyProgressSlider(
                player = player,
                onValueChange = {
                    isSeeking(true)
                },
                onValueChangeFinished = {
                    isSeeking(false)
                },

                )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PositionAndDurationText(player, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.weight(1f))
                PlaybackSpeedToggleButton(player, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary))
                ShuffleButton(player, colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary))
                if (isFullScreen != null) {
                    FullScreenButton(
                        isFullScreen = isFullScreen,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        onFullScreenChanged(!isFullScreen)
                    }
                } else {
                    RepeatButton(player, colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}

@Composable
fun FullScreenButton(isFullScreen: Boolean, colors: IconButtonColors, onClick: () -> Unit) {

    IconButton(onClick = onClick, colors = colors) {
        Icon(
            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
            contentDescription = if (isFullScreen) "Exit Fullscreen" else "Enter Fullscreen"
        )
    }
}



@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun MyProgressSlider(
    player: Player?,
    modifier: Modifier = Modifier,
    onValueChange: ((Float) -> Unit)? = null,
    onValueChangeFinished: (() -> Unit)? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val activeTrackColor = MaterialTheme.colorScheme.primary
    val inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
    val bufferedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
    val thumbColor = MaterialTheme.colorScheme.primary

    var sliderWidthPx by remember { mutableIntStateOf(0) }

    ProgressIndicator(player, totalTickCount = sliderWidthPx, scope) {
        var isDragging by remember { mutableStateOf(false) }
        var seekPosition by remember { mutableFloatStateOf(0f) }

        val sliderValue = if (isDragging) seekPosition else currentPositionProgress



        Slider(
            value = sliderValue,
            onValueChange = {
                isDragging = true
                seekPosition = it
                onValueChange?.invoke(it)
            },
            onValueChangeFinished = {
                updateCurrentPositionProgress(seekPosition)
                isDragging = false
                onValueChangeFinished?.invoke()
            },
            track = { sliderState ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(inactiveTrackColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(bufferedPositionProgress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(bufferedTrackColor)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(sliderState.value.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(activeTrackColor)
                    )
                }
            },
            thumb = {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(thumbColor)
                )
            },
            modifier = modifier.onSizeChanged { (w, _) -> sliderWidthPx = w },
            enabled = changingProgressEnabled,
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                disabledThumbColor = Color.Transparent,
                disabledActiveTrackColor = Color.Transparent,
                disabledInactiveTrackColor = Color.Transparent
            ),
            interactionSource = interactionSource,
        )
    }
}

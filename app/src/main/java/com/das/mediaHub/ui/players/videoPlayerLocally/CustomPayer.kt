package com.das.mediaHub.ui.players.videoPlayerLocally

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.material3.Player
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import kotlinx.coroutines.delay

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
        delay(5000L)
        if (!isSeeking) {
            isPlayerUiVisible = false
        }
    }

    LaunchedEffect(player, isPlaying, isSeeking) {
        while (isPlaying) {
            if (!isSeeking) {
                currentPosition = player.currentPosition.coerceAtLeast(0)
            }
            delay(16L)
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

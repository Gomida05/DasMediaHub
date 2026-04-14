package com.das.mediaHub.ui.players.videoPlayer.components

import android.app.Activity
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.core.net.toUri
import androidx.media3.exoplayer.ExoPlayer
import com.das.mediaHub.data.mediacontroller.online.VideoPlayerManager

@Composable
internal fun VideoPlayerEffects(
    window: Window?,
    videoId: String,
    videoUrl: String?,
    videoPlayerManager: VideoPlayerManager,
    exoPlayer: ExoPlayer,
    onInitialize: (String) -> Unit,
    onDisposePlayer: () -> Unit
) {
    LaunchedEffect(videoId) {
        onInitialize(videoId)
    }

    LaunchedEffect(videoUrl) {
        val url = videoUrl ?: return@LaunchedEffect
        videoPlayerManager.playVideo(videoId, url.toUri())
    }

    DisposableEffect(exoPlayer) {
        window?.setFlags(FLAG_KEEP_SCREEN_ON, FLAG_KEEP_SCREEN_ON)

        onDispose {
            onDisposePlayer()
        }
    }
}


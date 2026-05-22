package com.das.mediaHub.ui.players.videoPlayer.components

import android.net.Uri
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.retain.RetainedEffect
import androidx.core.net.toUri
import androidx.media3.common.Player
import com.das.mediaHub.data.mediacontroller.online.VideoPlayerManager

/**
 * Handles side effects for the Video Player, such as initializing loading,
 * playing videos when URLs are ready, and managing screen-on flags.
 */
@Composable
internal fun VideoPlayerEffects(
    window: Window?,
    videoId: String,
    videoUrl: String?,
    player: Player,
    onPlayNext: (String, Uri) -> Unit,
    onInitialize: (String) -> Unit,
    onDisposePlayer: () -> Unit
) {
    // 1. Initialize data loading when videoId changes
    LaunchedEffect(videoId) {
        onInitialize(videoId)
        
        // Optionally stop the player immediately when switching videos
        // to avoid showing the old video while loading the new one.
        if (player.currentMediaItem?.mediaId != videoId) {
            player.stop()
            player.clearMediaItems()
        }
    }

    // 2. Play video when URL is ready
    LaunchedEffect(videoUrl) {
        val url = videoUrl ?: return@LaunchedEffect
        onPlayNext(videoId, url.toUri())
    }

    // 3. Keep screen on while the player is active
    RetainedEffect (player) {
        window?.addFlags(FLAG_KEEP_SCREEN_ON)

        onRetire {
            window?.clearFlags(FLAG_KEEP_SCREEN_ON)
            onDisposePlayer()
        }
    }
}

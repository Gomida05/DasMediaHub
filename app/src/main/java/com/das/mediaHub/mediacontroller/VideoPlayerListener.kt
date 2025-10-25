package com.das.mediaHub.mediacontroller

import androidx.compose.material3.SnackbarHostState
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.navigation.NavController
import com.das.mediaHub.MainActivity
import com.das.mediaHub.PIP.shouldEnterPipMode
import com.das.mediaHub.WakeLockHelper.acquireWakeLock
import com.das.mediaHub.WakeLockHelper.releaseWakeLock
import com.das.mediaHub.ui.players.videoPlayer.playThisOne
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class VideoPlayerListener(
    val activity: MainActivity,
    private val navController: NavController,
    private val scope: CoroutineScope,
    private val snackBar: SnackbarHostState
) : Player.Listener {

    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        if (state == Player.STATE_ENDED) {
            shouldEnterPipMode = false
            playThisOne(navController,1)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        shouldEnterPipMode = false
        scope.launch {
            snackBar.showSnackbar("Something went wrong: ${error.message}")
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        shouldEnterPipMode = isPlaying
        if (isPlaying) {
            activity.acquireWakeLock()
        } else {
            activity.releaseWakeLock()
        }
    }
}


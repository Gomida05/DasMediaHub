package com.das.mediaHub.mediacontroller

import android.app.Activity
import androidx.compose.material3.SnackbarHostState
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.navigation.NavController
import com.das.mediaHub.WakeLockHelper
import com.das.mediaHub.ui.viewer.playThisOne
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PlayerListener(
    private val activity: Activity?,
    private val navController: NavController,
    private val scope: CoroutineScope,
    private val snackBar: SnackbarHostState
) : Player.Listener {
    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        if (state == Player.STATE_ENDED) {
            playThisOne(navController,1)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        scope.launch {
            snackBar.showSnackbar("Something went wrong: ${error.message}")
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            WakeLockHelper.acquireWakeLock(activity)
        } else {
            WakeLockHelper.releaseWakeLock(activity)
        }
    }
}


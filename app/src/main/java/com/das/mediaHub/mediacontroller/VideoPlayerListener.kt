package com.das.mediaHub.mediacontroller

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.MainActivity
import com.das.mediaHub.PIP.shouldEnterPipMode
import com.das.mediaHub.WakeLockHelper.acquireWakeLock
import com.das.mediaHub.WakeLockHelper.releaseWakeLock
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.players.videoPlayer.playThisOne

internal class VideoPlayerListener(
    val mainActivity: MainActivity,
    private val backStack: NavBackStack<NavKey>
) : Player.Listener {

    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        if (state == Player.STATE_ENDED) {
            shouldEnterPipMode.value = false
            playThisOne(backStack,1)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        shouldEnterPipMode.value = false
        showNotificationDialog = TopPopUp(
            message = "Something went wrong: ${error.message}",
            icon = Icons.Filled.Error
        )

    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        shouldEnterPipMode.value = isPlaying
        if (isPlaying) {
            mainActivity.acquireWakeLock()
        } else {
            mainActivity.releaseWakeLock()
        }
    }

}


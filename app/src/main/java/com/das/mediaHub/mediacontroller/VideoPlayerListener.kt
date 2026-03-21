package com.das.mediaHub.mediacontroller

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.PIP.canEnterPipMode
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.players.videoPlayer.playThisOne

internal class VideoPlayerListener(
    private val backStack: NavBackStack<NavKey>
) : Player.Listener {


    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        if (state == Player.STATE_ENDED) {
            canEnterPipMode.value = false
            playThisOne(backStack,0)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        canEnterPipMode.value = false
        showNotificationDialog = TopPopUp(
            message = "Something went wrong: ${error.message}",
            icon = Icons.Filled.Error
        )

    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        canEnterPipMode.value = isPlaying
    }

}


package com.das.mediaHub.data.mediacontroller.online

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.das.mediaHub.PIP
import com.das.mediaHub.PIP.canEnterPipMode
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog

internal class VideoPlayerListener(
    private val playNext: () -> Unit
) : Player.Listener {


    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        if (state == Player.STATE_ENDED) {
            PIP.isPlaybackActive = false
            PIP.allowAutoPip = false
            playNext()
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        showNotificationDialog = TopPopUp(
            message = "Something went wrong: ${error.message}",
            icon = Icons.Filled.Error
        )
        PIP.isPlaybackActive = false
        PIP.allowAutoPip = false

    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        PIP.isPlaybackActive = isPlaying
        PIP.allowAutoPip = isPlaying
    }

}


package com.das.mediaHub.data.mediacontroller.online

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.das.mediaHub.PIP
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.ui.notification.TopPopupNotification.showNotificationDialog

/**
 * Implementation of [Player.Listener] that handles playback lifecycle events, 
 * errors, and Picture-in-Picture state updates.
 *
 * @property playNext Callback triggered when the current media finishes playing.
 */
internal class VideoPlayerListener(
    private val playNext: () -> Unit
) : Player.Listener {

    /**
     * Updates PiP flags and triggers [playNext] when playback ends.
     */
    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        if (state == Player.STATE_ENDED) {
            PIP.isPlaybackActive = false
            PIP.allowAutoPip = false
            playNext()
        }
    }

    /**
     * Shows a popup notification when a player error occurs and resets PiP flags.
     */
    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        showNotificationDialog = TopPopUp(
            message = ErrorMapper.map(error),
            icon = Icons.Filled.Error
        )
        PIP.isPlaybackActive = false
        PIP.allowAutoPip = false
    }

    /**
     * Synchronizes the Picture-in-Picture system with the current playing state.
     */
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        PIP.isPlaybackActive = isPlaying
        PIP.allowAutoPip = isPlaying
    }
}

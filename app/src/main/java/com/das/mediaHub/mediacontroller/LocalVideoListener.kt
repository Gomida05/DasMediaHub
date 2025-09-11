package com.das.mediaHub.mediacontroller

import android.app.Activity
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.das.mediaHub.PIP.shouldEnterPipMode
import com.das.mediaHub.WakeLockHelper

class LocalVideoListener(
    private val activity: Activity?
) : Player.Listener {

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        shouldEnterPipMode = false
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        shouldEnterPipMode = isPlaying
        if (isPlaying) {
            WakeLockHelper.acquireWakeLock(activity)
        } else {
            WakeLockHelper.releaseWakeLock(activity)
        }
    }
}


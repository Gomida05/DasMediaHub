package com.das.mediaHub.mediacontroller

import android.app.Activity
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.das.mediaHub.PIP.shouldEnterPipMode
import com.das.mediaHub.WakeLockHelper.acquireWakeLock
import com.das.mediaHub.WakeLockHelper.releaseWakeLock

class LocalVideoListener(
    val activity: Activity
) : Player.Listener {

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        shouldEnterPipMode.value = false
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        shouldEnterPipMode.value = isPlaying
        if (isPlaying) {
            activity.acquireWakeLock()
        } else {
            activity.releaseWakeLock()
        }
    }
}


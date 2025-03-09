@file:Suppress("DEPRECATION")

package com.das.forui.mediacontroller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.das.forui.services.AudioServiceFromUrl

class ForUiMediaButtonReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val exoPlayer = AudioServiceFromUrl().exoPlayer
        val mediaSession = AudioServiceFromUrl().mediaSession
        val videoId = AudioServiceFromUrl().videoId
        val action = intent.action

        if (action == Intent.ACTION_MEDIA_BUTTON) {
            val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

            if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        if (exoPlayer?.isPlaying == true) {
                            exoPlayer.pause()
                            mediaSession.setPlaybackState(
                                MediaSessionPlaybackState(context).setStateToPaused(exoPlayer.currentPosition, videoId)
                            )
                        } else {
                            exoPlayer?.play()
                            mediaSession.setPlaybackState(
                                MediaSessionPlaybackState(context).setStateToPlaying(exoPlayer?.currentPosition!!, videoId)
                            )
                        }
                    }

                    KeyEvent.KEYCODE_MEDIA_NEXT -> {
                        exoPlayer?.seekToNext()
                    }

                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        exoPlayer?.seekToPrevious()
                    }
                }
            }
        }

    }
}

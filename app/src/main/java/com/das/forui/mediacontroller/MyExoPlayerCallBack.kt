package com.das.forui.mediacontroller

import android.content.Context
import android.media.session.MediaSession
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class MyExoPlayerCallBack(
    private val context: Context,
    private val exoPlayer: ExoPlayer,
    private val videoId: String,
    private val mediaSession: MediaSession
): Player.Listener {


    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
        val currentPosition = newPosition.positionMs

        mediaSession.setPlaybackState(
            MediaStateListener(context).setStateToPlaying(
                currentPosition, videoId
            )
        )
    }




    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)

        if (playbackState == Player.STATE_BUFFERING) {
            mediaSession.setPlaybackState(
                MediaStateListener(context)
                    .setStateToLoading(
                        exoPlayer.currentPosition, videoId
                    )
            )
            mediaSession.isActive = true
        } else if (playbackState == Player.STATE_ENDED) {
            mediaSession.setPlaybackState(
                MediaStateListener(context).setStateToPaused(
                    exoPlayer.currentPosition, videoId
                )
            )
            mediaSession.isActive = false
        }
    }


    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        mediaSession.setPlaybackState(
            MediaStateListener(context).setStateToLoading(
                error.timestampMs, videoId
            )
        )
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)

        if (isPlaying) {
            mediaSession.setPlaybackState(
                MediaStateListener(context).setStateToPlaying(
                    exoPlayer.currentPosition, videoId
                )
            )

        }

        if (!isPlaying && exoPlayer.isLoading) {
            mediaSession.setPlaybackState(
                MediaStateListener(context).setStateToLoading(
                    exoPlayer.currentPosition, videoId
                )
            )

        }

        if (!isPlaying) {

            mediaSession.setPlaybackState(
                MediaStateListener(context).setStateToPaused(
                    exoPlayer.currentPosition, videoId
                )
            )

        }
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        super.onIsLoadingChanged(isLoading)
        if (!isLoading) {

            mediaSession.setPlaybackState(
                MediaStateListener(context).setStateToPlaying(
                    exoPlayer.currentPosition, videoId
                )
            )

        } else {
            mediaSession.setPlaybackState(
                MediaStateListener(context).setStateToLoading(
                    exoPlayer.currentPosition, videoId
                )
            )
        }
    }

}

package com.das.mediaHub.mediacontroller

import android.support.v4.media.session.PlaybackStateCompat
import com.das.mediaHub.R
import com.das.mediaHub.data.constants.Action.ACTION_KILL
import com.das.mediaHub.data.constants.Playback.SET_SHUFFLE_MODE


object BackgroundPlayerStates {


    fun setStateToPlaying(currentPosition: Long, shuffleMode: Boolean): PlaybackStateCompat =
        buildPlaybackState(
            PlaybackStateCompat.STATE_PLAYING,
            currentPosition,
            shuffleMode,
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SEEK_TO
        )

    fun setStateToPaused(currentPosition: Long, shuffleMode: Boolean): PlaybackStateCompat =
        buildPlaybackState(
            PlaybackStateCompat.STATE_PAUSED,
            currentPosition,
            shuffleMode,
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SEEK_TO or
                    PlaybackStateCompat.ACTION_SET_REPEAT_MODE
        )

    fun setStateToLoading(currentPosition: Long, shuffleMode: Boolean): PlaybackStateCompat =
        buildPlaybackState(
            PlaybackStateCompat.STATE_BUFFERING,
            currentPosition,
            shuffleMode,
            PlaybackStateCompat.ACTION_PREPARE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SEEK_TO
        )


    private fun buildPlaybackState(
        state: Int,
        currentPosition: Long,
        shuffleMode: Boolean,
        actions: Long
    ): PlaybackStateCompat {
        return PlaybackStateCompat.Builder()
            .setState(state, currentPosition, 1F)
            .setActions(actions)
            .addCustomAction(
                SET_SHUFFLE_MODE,
                "mySHUFFLEButton",
                if (shuffleMode) R.drawable.shuffle_icon_on else R.drawable.shuffle_icon_off
            )
            .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
            .setBufferedPosition(currentPosition)
            .build()
    }
}

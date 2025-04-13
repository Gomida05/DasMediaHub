package com.das.forui.mediacontroller


import android.support.v4.media.session.PlaybackStateCompat
import com.das.forui.R
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_KILL
import com.das.forui.objectsAndData.ForUIKeyWords.SET_SHUFFLE_MODE

object BackgroundPlayerStates {


    fun setStateToPlaying(currentPosition: Long, shuffleMode: Boolean): PlaybackStateCompat {
        val playbackState = PlaybackStateCompat.Builder()
            .setState(
                PlaybackStateCompat.STATE_PLAYING, currentPosition,
                1F
            )
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .addCustomAction(
                SET_SHUFFLE_MODE, "mySHUFFLEButton",
                if (shuffleMode) R.drawable.shuffle_icon_on else R.drawable.shuffle_icon_off
            )
            .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
            .setBufferedPosition(currentPosition)
            .build()

        return playbackState
    }

    fun setStateToPaused(currentPosition: Long, shuffleMode: Boolean): PlaybackStateCompat{
        val playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PAUSED, currentPosition,
                1F
            )
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_SET_REPEAT_MODE
            )
            .addCustomAction(
                SET_SHUFFLE_MODE, "mySHUFFLEButton",
                if (shuffleMode) R.drawable.shuffle_icon_on else R.drawable.shuffle_icon_off
            )
            .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
            .setBufferedPosition(currentPosition)
            .build()

        return playbackState
    }

    fun setStateToLoading(currentPosition: Long, shuffleMode: Boolean): PlaybackStateCompat {
        val playbackState = PlaybackStateCompat.Builder()
            .setState(
                PlaybackStateCompat.STATE_BUFFERING, currentPosition,
                1F
            )
            .setActions(
                PlaybackStateCompat.ACTION_PREPARE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .addCustomAction(
                SET_SHUFFLE_MODE, "mySHUFFLEButton",
                if (shuffleMode) R.drawable.shuffle_icon_on else R.drawable.shuffle_icon_off
            )
            .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
            .setBufferedPosition(currentPosition)
            .build()

        return playbackState
    }


}

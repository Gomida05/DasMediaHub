package com.das.forui.mediacontroller

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import com.das.forui.R
import com.das.forui.databased.DatabaseFavorite
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_ADD_TO_WATCH_LATER
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_KILL
import com.das.forui.ui.viewer.ViewerFragment.Video

class MediaSessionPlaybackState(private val context: Context) {


    fun setStateToPlaying(currentPosition: Long, videoId: String): PlaybackStateCompat {
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
                ACTION_ADD_TO_WATCH_LATER, "myFavButton",
                if (isAddedToTheDataBased(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon
            )
            .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
            .setBufferedPosition(currentPosition)
            .build()

        return playbackState
    }

    fun setStateToPaused(currentPosition: Long, videoId: String): PlaybackStateCompat{
        val playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PAUSED, currentPosition,
                1F
            )
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton",
                if (isAddedToTheDataBased(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon
            )
            .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
            .setBufferedPosition(currentPosition)
            .build()

        return playbackState
    }

    fun setStateToLoading(currentPosition: Long, videoId: String): PlaybackStateCompat {
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
                ACTION_ADD_TO_WATCH_LATER, "myFavButton",
                if (isAddedToTheDataBased(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon
            )
            .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
            .setBufferedPosition(currentPosition)
            .build()

        return playbackState
    }

    fun addItOrRemoveFromDB(
        currentPosition: Long,
        video: Video
    ): PlaybackStateCompat{
        val db = DatabaseFavorite(context)
        val playbackSate = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PLAYING, currentPosition,
                1F
            )
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setBufferedPosition(currentPosition)
        if (isAddedToTheDataBased(video.videoId)){
            db.deleteWatchUrl(video.videoId)
            playbackSate
                .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton", R.drawable.un_favorite_icon)
                .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)

        }
        else{
            db.insertData(
                video.videoId, video.title, video.dateOfVideo,
                video.views, video.channelName, video.duration
            )
            playbackSate
                .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton", R.drawable.favorite)
                .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)

        }

        return playbackSate.build()
    }


    private fun isAddedToTheDataBased(videoId: String): Boolean{
        println("the provided url is ${DatabaseFavorite(context).isWatchUrlExist(videoId)}")
        return DatabaseFavorite(context).isWatchUrlExist(videoId)

    }

}

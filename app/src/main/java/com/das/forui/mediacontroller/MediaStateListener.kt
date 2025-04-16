package com.das.forui.mediacontroller

import android.content.Context
import android.media.session.PlaybackState
import com.das.forui.R
import com.das.forui.databased.DatabaseFavorite
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_ADD_TO_WATCH_LATER
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_KILL
import com.das.forui.objectsAndData.ForUIDataClass.VideosListData

class MediaStateListener(private val context: Context) {


    fun setStateToPlaying(currentPosition: Long, videoId: String): PlaybackState {
        val playbackState = PlaybackState.Builder()
            .setState(
                PlaybackState.STATE_PLAYING, currentPosition,
                1F
            )
            .setActions(
                PlaybackState.ACTION_PLAY_PAUSE or
                        PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackState.ACTION_SEEK_TO
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

    fun setStateToPaused(currentPosition: Long, videoId: String): PlaybackState {
        val playbackState = PlaybackState.Builder()
            .setState(
                PlaybackState.STATE_PAUSED, currentPosition,
                1F
            )
            .setActions(
                PlaybackState.ACTION_PLAY_PAUSE or
                        PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackState.ACTION_SEEK_TO
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

    fun setStateToLoading(currentPosition: Long, videoId: String): PlaybackState {
        val playbackState = PlaybackState.Builder()
            .setState(
                PlaybackState.STATE_BUFFERING, currentPosition,
                1F
            )
            .setActions(
                PlaybackState.ACTION_PREPARE or
                        PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackState.ACTION_SEEK_TO
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
        videosListData: VideosListData
    ): PlaybackState {
        val db = DatabaseFavorite(context)
        val playbackSate = PlaybackState.Builder()
            .setState(
                PlaybackState.STATE_PLAYING, currentPosition,
                1F
            )
            .setActions(
                PlaybackState.ACTION_PLAY_PAUSE or
                        PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackState.ACTION_SEEK_TO
            )
            .setBufferedPosition(currentPosition)
        if (isAddedToTheDataBased(videosListData.videoId)){
            db.deleteWatchUrl(videosListData.videoId)
            playbackSate
                .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton", R.drawable.un_favorite_icon)
                .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)

        }
        else{
            db.insertData(
                videosListData.videoId, videosListData.title, videosListData.dateOfVideo,
                videosListData.views, videosListData.channelName, videosListData.duration,
                videosListData.channelThumbnailsUrl)
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
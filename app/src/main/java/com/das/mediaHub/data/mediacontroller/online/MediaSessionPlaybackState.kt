package com.das.mediaHub.data.mediacontroller.online

import android.support.v4.media.session.PlaybackStateCompat
import com.das.mediaHub.R
import com.das.mediaHub.data.constants.Action.ACTION_ADD_TO_WATCH_LATER
import com.das.mediaHub.data.constants.Action.ACTION_KILL
import com.das.mediaHub.data.repository.FavoritesRepository
import com.das.python.data.model.VideosListData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class MediaSessionPlaybackState(private val db: FavoritesRepository) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        currentPosition: Long, videosListData: VideosListData
    ): PlaybackStateCompat{
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
        if (isAddedToTheDataBased(videosListData.videoId)){

            scope.launch {
                db.deleteWatchUrl(videosListData.videoId)
            }
            playbackSate
                .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton", R.drawable.un_favorite_icon)
                .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)

        }
        else{
            scope.launch {
                db.insertData(
                    videosListData.videoId, videosListData.title, videosListData.dateOfVideo,
                    videosListData.views, videosListData.channelName, videosListData.duration,
                    videosListData.channelThumbnailsUrl)
            }
            playbackSate
                .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton", R.drawable.favorite)
                .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)

        }

        return playbackSate.build()
    }


    private fun isAddedToTheDataBased(videoId: String): Boolean {
        return db.isWatchUrlExist(videoId)
            .stateIn(scope, started = SharingStarted.WhileSubscribed(300), initialValue = false)
            .value
    }

}

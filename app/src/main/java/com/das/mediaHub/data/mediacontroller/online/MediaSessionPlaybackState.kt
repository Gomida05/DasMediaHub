package com.das.mediaHub.data.mediacontroller.online

import android.annotation.SuppressLint
import androidx.media3.session.legacy.PlaybackStateCompat
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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for building and updating the legacy [PlaybackStateCompat] used by MediaSession.
 *
 * This class handles the construction of the playback state, including standard transport 
 * controls and custom actions like "Add to Watch Later" and "Kill Service".
 */
@SuppressLint("RestrictedApi")
@Singleton
class MediaSessionPlaybackState @Inject constructor(
    private val db: FavoritesRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Builds a [PlaybackStateCompat] for the 'Playing' state.
     * 
     * @param currentPosition Current playback position.
     * @param videoId ID of the current video.
     */
    fun setStateToPlaying(currentPosition: Long, videoId: String): PlaybackStateCompat {
        return PlaybackStateCompat.Builder()
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
    }

    /**
     * Builds a [PlaybackStateCompat] for the 'Paused' state.
     */
    fun setStateToPaused(currentPosition: Long, videoId: String): PlaybackStateCompat{
        return PlaybackStateCompat.Builder()
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
    }

    /**
     * Builds a [PlaybackStateCompat] for the 'Buffering' state.
     */
    fun setStateToLoading(currentPosition: Long, videoId: String): PlaybackStateCompat {
        return PlaybackStateCompat.Builder()
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
    }

    /**
     * Updates the database and returns an updated [PlaybackStateCompat] when the 
     * user toggles the favorite status from the notification/lock screen.
     */
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


    /**
     * Checks if a video exists in the database.
     */
    private fun isAddedToTheDataBased(videoId: String): Boolean {
        return db.isWatchUrlExist(videoId)
            .stateIn(scope, started = SharingStarted.WhileSubscribed(300), initialValue = false)
            .value
    }
}

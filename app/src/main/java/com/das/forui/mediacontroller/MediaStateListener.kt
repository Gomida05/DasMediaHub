package com.das.forui.mediacontroller

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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








class MyMediaSessionCallBack(
    private val context: Context,
    private val mediaDetails: VideosListData,
    private val mediaSession: MediaSession,
    private val exoPlayer: ExoPlayer
): MediaSession.Callback() {
    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
        exoPlayer.seekTo(pos)

    }

    override fun onPlay() {
        super.onPlay()
        mediaSession.isActive = true
        exoPlayer.play()
    }

    override fun onPause() {
        super.onPause()
        mediaSession.isActive = false
        exoPlayer.pause()
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        exoPlayer.seekToPrevious()
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        exoPlayer.seekToNext()
    }


    override fun onCustomAction(action: String, extras: Bundle?) {

        if (action == ACTION_ADD_TO_WATCH_LATER) {

            mediaSession.setPlaybackState(
                MediaStateListener(context).addItOrRemoveFromDB(
                    exoPlayer.currentPosition,
                    mediaDetails
                )
            )
        }
        else if (action == ACTION_KILL){
            exoPlayer.let {
                it.stop()
                it.release()
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.cancel(1)

        }
    }

    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
        mediaButtonIntent.let {
            @Suppress("DEPRECATION")
            val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                it.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            else it.getParcelableExtra(Intent.EXTRA_KEY_EVENT)

            if (it.action == Intent.ACTION_MEDIA_BUTTON) {
                // Extract the key event from the intent
                keyEvent?.let { event ->
                    when (event.keyCode) {

                        KeyEvent.KEYCODE_MEDIA_PAUSE ->{
                            exoPlayer.pause()
                        }

                        KeyEvent.KEYCODE_MEDIA_PLAY ->{
                            exoPlayer.play()
                        }

                        KeyEvent.KEYCODE_MEDIA_NEXT -> {
                            onSkipToNext()
                            return true
                        }

                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                            exoPlayer.seekToPrevious()
                            return true
                        }

                        else -> {
                            return true
                        }
                    }
                }
            }
        }
        // If the event is not handled, call the superclass method
        return super.onMediaButtonEvent(mediaButtonIntent)


    }

}






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

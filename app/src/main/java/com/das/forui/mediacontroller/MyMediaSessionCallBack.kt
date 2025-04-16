package com.das.forui.mediacontroller

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.media3.exoplayer.ExoPlayer
import com.das.forui.objectsAndData.ForUIDataClass.VideosListData
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_ADD_TO_WATCH_LATER
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_KILL

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



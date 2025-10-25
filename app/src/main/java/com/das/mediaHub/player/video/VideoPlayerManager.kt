package com.das.mediaHub.player.video

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Rational
import android.view.KeyEvent
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.das.mediaHub.data.constants.Action.ACTION_KILL
import com.das.mediaHub.data.constants.Playback.PAUSE
import com.das.mediaHub.data.constants.Playback.PLAY
import com.das.mediaHub.mediacontroller.VideoPlayerListener

class VideoPlayerManager(
    private val context: Context,
    private val playerListener: VideoPlayerListener
): PlayerController {

    private val myMediaSession = MediaSessionCompat(context, "VideoPlayer").apply {
        isActive = true
        @Suppress("DEPRECATION")
        setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        setMediaButtonReceiver(
            PendingIntent.getBroadcast(
                context, 0,
                Intent(Intent.ACTION_MEDIA_BUTTON),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

    }

    override val player by lazy {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(AUDIO_CONTENT_TYPE_MOVIE)
                    .build(), true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
    }


    val isEmptyMediaItem = player.currentMediaItem == null
    val isPlaying = mutableStateOf(player.isPlaying)

    override fun addListener() {
        player.addListener(
            playerListener
        )
        myMediaSession.apply {
            setCallback(MyMediaSessionCallBack())
            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(
                        if (isPlaying.value) PlaybackStateCompat.STATE_PLAYING else
                            PlaybackStateCompat.STATE_PAUSED, player.currentPosition,
                        1F
                    )
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                PlaybackStateCompat.ACTION_SEEK_TO
                    )
                    .addCustomAction(
                        if (isPlaying.value) PAUSE else PLAY, "play OR pause",
                        if (isPlaying.value) androidx.media3.session.R.drawable.media3_icon_pause else androidx.media3.session.R.drawable.media3_icon_play
                    )
                    .setBufferedPosition(player.currentPosition)
                    .build()
            )
        }
    }

    private fun addMediaItem(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        myMediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Unknown")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Unknown")

                .putString(
                    MediaMetadataCompat.METADATA_KEY_ALBUM,
                    "unknown album"
                )
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.duration)
                .build()
        )
        player.setMediaItem(mediaItem)
    }

    override fun playVideo(url: Uri) {

        if (player.currentMediaItem != null) {
            player.prepare()
            addListener()
            resume()
        } else {
            addMediaItem(url)
            player.prepare()
            addListener()
            resume()
        }
    }

    override fun pause() {
        player.pause()
    }

    override fun resume() {
        player.play()
    }

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    override fun release() {
        player.release()
    }

    private fun updatePipActions() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            playerListener.activity.setPictureInPictureParams(params)
        }
    }


    private inner class MyMediaSessionCallBack : MediaSessionCompat.Callback() {


        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            player.seekTo(pos)
            updatePipActions()
        }

        override fun onPlay() {
            super.onPlay()
            myMediaSession.isActive = true
            resume()
            updatePipActions()
        }

        override fun onPause() {
            super.onPause()
            myMediaSession.isActive = false
            pause()
            updatePipActions()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            player.seekToPrevious()
            updatePipActions()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            player.seekToNext()
            updatePipActions()
        }


        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
            if (action.toString() == ACTION_KILL) {
                release()
                updatePipActions()
            }
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            mediaButtonEvent?.let {
                @Suppress("DEPRECATION")
                val keyEvent = if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU)
                    it.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                else it.getParcelableExtra(Intent.EXTRA_KEY_EVENT)

                if (it.action == Intent.ACTION_MEDIA_BUTTON) {
                    // Extract the key event from the intent
                    keyEvent?.let { event ->
                        when (event.keyCode) {

                            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                player.pause()
                            }

                            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                player.play()
                            }

                            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                                onSkipToNext()
                                return true
                            }

                            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                                player.seekToPrevious()
                                return true
                            }

                            else -> {
                                return true
                            }
                        }
                    }
                    updatePipActions()
                }
            }
            // If the event is not handled, call the superclass method
            return super.onMediaButtonEvent(mediaButtonEvent)


        }
    }
}

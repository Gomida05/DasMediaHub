package com.das.mediaHub.player.video

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
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
import androidx.media3.session.R
import com.das.mediaHub.PIP
import com.das.mediaHub.PIP.getPipSourceRect
import com.das.mediaHub.data.constants.Action.ACTION_KILL
import com.das.mediaHub.data.constants.Playback.PAUSE
import com.das.mediaHub.data.constants.Playback.PLAY
import com.das.mediaHub.mediacontroller.VideoPlayerListener

@SuppressLint("UnsafeOptInUsageError")
internal class VideoPlayerManager(
    private val activity: Activity,
    private val playerListener: VideoPlayerListener
): PlayerController {
    val applicationContext: Context = activity.applicationContext

    private val myMediaSession = MediaSessionCompat(applicationContext, "VideoPlayer").apply {
        isActive = true

        setMediaButtonReceiver(
            PendingIntent.getBroadcast(
                applicationContext, 0,
                Intent(Intent.ACTION_MEDIA_BUTTON),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

    }

    override val player by lazy<ExoPlayer> {
        ExoPlayer.Builder(applicationContext)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(AUDIO_CONTENT_TYPE_MOVIE)
                    .build(), true
            )
            .setWakeMode(C.WAKE_MODE_NONE)
            .setPriority(C.PRIORITY_PLAYBACK)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }


    val isPlaying = mutableStateOf(value = player.isPlaying)

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
                        if (isPlaying.value) R.drawable.media3_icon_pause else R.drawable.media3_icon_play
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
        PIP.canEnterPipMode.value = false
    }

    private fun updatePipActions() {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))

        if (SDK_INT >= VERSION_CODES.S) {
            params
                .setAutoEnterEnabled(true)
                .setSeamlessResizeEnabled(true)
                .setSourceRectHint(activity.getPipSourceRect())
        }
        activity.setPictureInPictureParams(params.build())
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
            }
            updatePipActions()
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            if (mediaButtonEvent == null) return super.onMediaButtonEvent(mediaButtonEvent)

            @Suppress("DEPRECATION")
            val keyEvent = if (SDK_INT >= VERSION_CODES.TIRAMISU)
                mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            else mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

            if (mediaButtonEvent.action == Intent.ACTION_MEDIA_BUTTON && keyEvent != null) {
                // Extract the key event from the intent
                when (keyEvent.keyCode) {

                    KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                        player.pause()
                    }

                    KeyEvent.KEYCODE_MEDIA_PLAY -> {
                        player.play()
                    }

                    KeyEvent.KEYCODE_MEDIA_NEXT -> {
                        onSkipToNext()
                    }

                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        player.seekToPrevious()
                    }
                }
                updatePipActions()
                return true
            }
            return super.onMediaButtonEvent(mediaButtonEvent)

        }
    }
}

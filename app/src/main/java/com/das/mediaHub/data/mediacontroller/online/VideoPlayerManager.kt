package com.das.mediaHub.data.mediacontroller.online

import android.app.PictureInPictureParams
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Rational
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.das.mediaHub.PIP
import com.das.mediaHub.PIP.findActivity
import com.das.mediaHub.PIP.getPipSourceRect

internal class VideoPlayerManager(private val applicationContext: Context): PlayerController {



    override val player by lazy {
        ExoPlayer.Builder(applicationContext)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(), true
            )
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    override fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        player.removeListener(listener)
    }

    private fun addMediaItem(videoId: String, uri: Uri) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(videoId)
            .setUri(uri)
            .build()

        player.setMediaItem(mediaItem)
    }

    override fun playVideo(videoId: String, uri: Uri) {
        addMediaItem(videoId = videoId, uri = uri)
        player.prepare()
        resume()
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
        player.clearMediaItems()
        PIP.isPlaybackActive = false
        PIP.allowAutoPip = false

    }

    private fun updatePipActions() {
        applicationContext.findActivity()?.let { activity ->
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                params
                    .setAutoEnterEnabled(true)
                    .setSeamlessResizeEnabled(true)
                    .setSourceRectHint(activity.getPipSourceRect())
            }
            activity.setPictureInPictureParams(params.build())
        }
    }

    override fun close() {
        release()
        player.release()
    }


}
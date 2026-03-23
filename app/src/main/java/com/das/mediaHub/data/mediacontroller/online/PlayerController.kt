package com.das.mediaHub.data.mediacontroller.online

import android.net.Uri
import androidx.media3.exoplayer.ExoPlayer

interface PlayerController {

    val player: ExoPlayer

    fun playVideo(url: Uri)

    fun addListener()

    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun release()
}
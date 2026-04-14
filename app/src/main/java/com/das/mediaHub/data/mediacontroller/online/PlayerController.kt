package com.das.mediaHub.data.mediacontroller.online

import android.net.Uri
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

interface PlayerController {

    val player: ExoPlayer

    fun playVideo(videoId: String,uri: Uri)

    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun release()
    fun addListener(listener: Player.Listener)

    fun removeListener(listener: Player.Listener)

    fun close()
}
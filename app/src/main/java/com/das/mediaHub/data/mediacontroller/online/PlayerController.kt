package com.das.mediaHub.data.mediacontroller.online

import android.net.Uri
import androidx.media3.common.Player

/**
 * Interface defining the core control operations for a video or audio player.
 *
 * It abstracts the interaction with the underlying [Player] (e.g., ExoPlayer) 
 * to provide a high-level API for UI components.
 *
 * Example usage:
 * ```kotlin
 * class MyPlayer : PlayerController { ... }
 * ```
 */
interface PlayerController {

    /** The underlying [Player] instance. */
    val player: Player

    /**
     * Plays a video from a specific URI.
     * 
     * @param videoId Unique identifier for the video.
     * @param uri The URI to play.
     */
    fun playVideo(videoId: String, uri: Uri)

    /** Pauses the current playback. */
    fun pause()
    
    /** Resumes the current playback. */
    fun resume()
    
    /**
     * Seeks to a specific position in the media.
     * @param positionMs Position in milliseconds.
     */
    fun seekTo(positionMs: Long)
    
    /** Stops and clears the currently loaded media. */
    fun closeCurrentlyMedia()
    
    /** Adds a listener to the player. */
    fun addListener(listener: Player.Listener)

    /** Removes a listener from the player. */
    fun removeListener(listener: Player.Listener)

    /** Releases all resources associated with the player. */
    fun release()
}

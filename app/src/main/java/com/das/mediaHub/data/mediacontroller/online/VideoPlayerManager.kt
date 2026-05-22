package com.das.mediaHub.data.mediacontroller.online

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.das.mediaHub.PIP
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [PlayerController] responsible for managing online video playback.
 *
 * It uses Hilt for dependency injection to provide a [Singleton] player instance.
 * It also handles Picture-in-Picture (PiP) state management and automatically 
 * fetches YouTube thumbnails for notifications.
 *
 * Example usage:
 * ```kotlin
 * @Inject
 * lateinit var videoPlayerManager: VideoPlayerManager
 * videoPlayerManager.playVideo("videoId", videoUri)
 * ```
 */
@Singleton
class VideoPlayerManager @Inject constructor(
    override val player: Player
): PlayerController {

    /** Current playback position in milliseconds. */
    val currentPosition: Long
        get() = player.currentPosition.coerceAtLeast(0)

    override fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        player.removeListener(listener)
    }

    /**
     * Builds and sets a [MediaItem] with metadata for the given video.
     */
    private fun addMediaItem(videoId: String, uri: Uri) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(videoId)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(videoId)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                    .setArtworkUri("https://img.youtube.com/vi/$videoId/0.jpg".toUri())
                    .build()
            )
            .setUri(uri)
            .build()
        player.setMediaItem(mediaItem)
    }

    /**
     * Loads and plays a video. If the video is already loaded, it resumes playback.
     */
    override fun playVideo(videoId: String, uri: Uri) {
        val currentItem = player.currentMediaItem

        if (currentItem?.mediaId == videoId) {
            // Already loaded → just resume if needed
            if (!player.isPlaying) {
                player.play()
            }
            return
        }

        // New video → load it
        addMediaItem(videoId = videoId, uri = uri)
        player.prepare()
        player.play()
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

    /**
     * Clears the current media and resets Picture-in-Picture flags.
     */
    override fun closeCurrentlyMedia() {
        player.clearMediaItems()
        PIP.isPlaybackActive = false
        PIP.allowAutoPip = false
    }

    override fun release() {
        closeCurrentlyMedia()
        player.release()
    }
}

package com.das.downloader.data.downloader

import android.os.Parcelable
import com.das.downloader.data.model.download.DownloadType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * A sealed class representing the various types of download requests supported by the application.
 *
 * By encapsulating the specific parameters required for different media sources (YouTube,
 * Social Media, or APK updates), this class allows the `DownloadCoordinator` to process
 * them uniformly. It implements [Parcelable] so that requests can be safely passed
 * between Android components (e.g., Intents, Bundles, or Navigation arguments).
 */
@Serializable
sealed class DownloadRequest : Parcelable {
    abstract val title: String

    /**
     * Represents a request to download a video stream from YouTube.
     *
     * @property videoId The unique 11-character identifier of the YouTube video.
     * @property title The desired title or filename for the downloaded video.
     */
    @Parcelize
    @Serializable
    data class YoutubeVideo(
        val videoId: String,
        override val title: String
    ) : DownloadRequest()

    /**
     * Represents a request to extract and download the audio stream from a YouTube video.
     *
     * @property videoId The unique 11-character identifier of the YouTube video.
     * @property title The desired title or filename for the downloaded audio track.
     */
    @Parcelize
    @Serializable
    data class YoutubeAudio(
        val videoId: String,
        override val title: String
    ) : DownloadRequest()

    /**
     * Represents a request to download media from a supported social media platform
     * (e.g., TikTok or Instagram).
     *
     * @property url The full public URL to the social media post or video.
     * @property title The desired title or filename for the downloaded media.
     * @property downloadType The specific [DownloadType] indicating the platform and file extension.
     */
    @Parcelize
    @Serializable
    data class Social(
        val url: String,
        override val title: String,
        val downloadType: DownloadType
    ) : DownloadRequest()

}

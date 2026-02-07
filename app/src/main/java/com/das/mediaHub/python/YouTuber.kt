package com.das.mediaHub.python

import androidx.compose.runtime.mutableStateListOf
import androidx.media3.common.MediaItem
import com.das.mediaHub.data.constants.YouTubeRegexes
import com.das.mediaHub.data.model.ItemsStreamUrlsForMediaItemData
import com.das.mediaHub.data.model.PlayListDataClass
import com.das.mediaHub.data.model.VideosListData
import com.das.mediaHub.python.PythonMain.getPlayListUrl
import com.das.mediaHub.python.data.Names.GET_AUDIO_STREAM_URL
import com.das.mediaHub.python.data.Names.GET_VIDEO_STREAM_URL
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

internal object YouTuber {
    val mediaItems = mutableStateListOf<MediaItem>()

    fun List<MediaItem>.updateGlobalMediaItems() {
        mediaItems.clear()
        mediaItems.addAll(this)
    }

    /**
     * Extracts the YouTube video ID from a given URL using a predefined regex.
     *
     * @param url Full YouTube URL (e.g., "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
     * @return Video ID if found, or null if extraction fails.
     */
    fun String.youtubeExtractor(): String? {

        val pattern = Regex(YouTubeRegexes.YOUTUBE_REGEX)
        val match = pattern.find(input = this)
        return match?.groups?.get(1)?.value
    }

    /**
     * Returns data format like this dd/MMM/yyyy ENGLISH
     */
    fun String.formatDate(): String {
        if (isBlank() || equals("None", ignoreCase = true)) return ""

        if (contains("ago", ignoreCase = true)) return this
        return try {

            val formatter = when {
                contains("T") && contains("Z") ->
                    DateTimeFormatter.ISO_INSTANT

                contains("T") && contains("+") ->
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME

                length == 10 && this[4] == '-' ->
                    DateTimeFormatter.ISO_LOCAL_DATE

                length == 4 && all { it.isDigit() } ->
                    return this

                else -> return this
            }

            val date = when (val temporal = formatter.parse(this)) {
                is Instant -> temporal.atZone(ZoneId.systemDefault())
                else -> ZonedDateTime.from(temporal)
            }

            date.format(
                DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
            )

        } catch (_: Exception) {
            this
        }
    }

    /**
     * Validates if the provided URL is a valid YouTube video link.
     *
     * Supports:
     * - Standard YouTube URLs (youtube.com/watch?v=...)
     * - Shortened URLs (youtu.be/VIDEO_ID)
     *
     * @return true if the URL is a valid YouTube video link, false otherwise.
     */
    fun String.isValidYoutubeURL(): Boolean {
        return try {
            val trimmedUrl = trim().removeSuffix("&feature=shared")

            val url = URL(trimmedUrl)

            val host = url.host
            when (host) {
                YouTubeRegexes.YOUTUBE_HOST_1, YouTubeRegexes.YOUTUBE_HOST_2 -> {
                    val queryParams = url.query
                        ?.split("&")
                        ?.mapNotNull {
                            val parts = it.split("=")
                            if (parts.size == 2) parts[0] to parts[1] else null
                        }
                        ?.toMap()

                    val videoId = queryParams?.get("v")
                    videoId?.matches(Regex("[A-Za-z0-9_-]{11}")) == true
                }

                YouTubeRegexes.YOUTUBE_HOST_3 -> {
                    val videoId = url.path.removePrefix("/")
                    videoId.matches(Regex("[A-Za-z0-9_-]{11}"))
                }

                else -> {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    fun String.isValidYouTubePlaylistUrl(): Boolean {
        val regex = Regex(""".*?(youtube\.com|youtu\.be).*[?&]list=([a-zA-Z0-9_-]+)""")
        return regex.containsMatchIn(this)
    }

    fun extractPlaylistId(url: String): String? {
        val regex = Regex(""".*[?&]list=([a-zA-Z0-9_-]+)""")
        return regex.find(url)?.groupValues?.get(1)
    }

    suspend fun VideosListData.loadStreamUrl(
        onSuccess: (ItemsStreamUrlsForMediaItemData) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val result = PythonMain.getStreamUrl(
                type = GET_AUDIO_STREAM_URL,
                id = videoId
            )

            if (result.success && result.result != null) {
                onSuccess(
                    ItemsStreamUrlsForMediaItemData(
                    result.result,
                    videoId,
                    title,
                    views,
                    dateOfVideo,
                    duration,
                    channelName,
                    channelThumbnailsUrl
                )
                )
            } else {
                onFailure(Exception("Something went wrong: $result"))
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }


    fun formatDateFromLong(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    /**
     * Converts a number of views into a shortened string format:
     * - 1,200 → 1.2K
     * - 1,200,000 → 1.2M
     * - 1,200,000,000 → 1.2B
     *
     * @return Formatted string with K, M, or B suffix
     */
    fun String.formatViews(): String {
        val digitsOnly = this
            .lowercase()
            .replace("views", "")
            .replace(",", "")
            .trim()

        val viewsLong = digitsOnly.toLongOrNull() ?: return this

        return when {
            viewsLong >= 1_000_000_000 -> "%.1fB".format(viewsLong / 1_000_000_000.0)
            viewsLong >= 1_000_000 -> "%.1fM".format(viewsLong / 1_000_000.0)
            viewsLong >= 1_000 -> "%.1fK".format(viewsLong / 1_000.0)
            else -> "$viewsLong"
        }
    }


    suspend fun getVideoStreamUrl(
        videoId: String,
        onSuccess: (streamUrl: String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val result = PythonMain.getStreamUrl(
                type = GET_VIDEO_STREAM_URL,
                id = videoId
            )

            if (result.success && !result.result.isNullOrEmpty()) {
                onSuccess(result.result)
            } else {
                onFailure(result.error.toString())
            }
        } catch (e: Exception) {
            onFailure("Something went wrong with result: ${e.message}")
        }
    }

    suspend fun getAudioStreamUrl(
        videoId: String,
        onSuccess: (streamUrl: String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val result = PythonMain.getStreamUrl(
                type = GET_AUDIO_STREAM_URL,
                id = videoId
            )

            if (result.success && !result.result.isNullOrEmpty()) {
                onSuccess(result.result)
            } else {
                onFailure(result.error.toString())
            }

        } catch (e: Exception) {
            onFailure("Something went wrong with result: ${e.message}")
        }
    }

    suspend fun getPlayListStreamUrl(
        playListUrl: String,
        onSuccess: (playListName: String, videoList: List<PlayListDataClass>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val result = getPlayListUrl(playListUrl)

            if (result.isNotEmpty()) {
                // Switch to the main thread for UI updates
                onSuccess(
                    "Testing PLayList Downloader",
                    result
                )
            } else {
                onFailure("Something went wrong with result: $result")
            }
        } catch (e: Exception) {
            onFailure("Something went wrong with result: ${e.message}")
        }

    }

}
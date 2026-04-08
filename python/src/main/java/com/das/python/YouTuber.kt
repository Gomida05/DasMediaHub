package com.das.python

import com.das.python.data.constants.YouTubeRegexes
import com.das.python.data.model.ItemsStreamUrlsForMediaItemData
import com.das.python.data.model.PlayListDataClass
import com.das.python.data.model.responds.ResponseVideo
import com.das.python.data.model.VideosListData
import com.das.python.data.model.responds.RespondVideoDetails
import com.das.python.exceptions.PyCallError
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Utility object providing helper and extension functions
 * for working with YouTube URLs, metadata formatting,
 * and stream extraction.
 *
 * This object is designed to be used as part of the library layer.
 * All network-related operations are suspend functions.
 */
object YouTuber {

    private val py = MainPyImpl()

    /**
     * Extracts a YouTube video ID from a given URL string.
     *
     * Supports standard and shortened YouTube links.
     *
     * Example:
     * https://www.youtube.com/watch?v=dQw4w9WgXcQ
     * → dQw4w9WgXcQ
     *
     * @receiver Full YouTube URL.
     * @return The extracted video ID, or null if not found.
     */
    fun String.youtubeExtractor(): String? {
        val pattern = Regex(YouTubeRegexes.YOUTUBE_REGEX)
        val match = pattern.find(this) ?: return null
        return match
            .groups[1]
            ?.value
            ?.substringBefore("&")
            ?.substringBefore("?")
    }

    /**
     * Formats various ISO date formats into:
     * "dd MMM yyyy" (English locale).
     *
     * Supported inputs:
     * - ISO_INSTANT
     * - ISO_OFFSET_DATE_TIME
     * - ISO_LOCAL_DATE
     * - Year only (returns unchanged)
     *
     * If the input contains "ago" or is invalid,
     * the original string is returned.
     *
     * @receiver Raw date string.
     * @return Formatted date string.
     */
    fun String.formatDate(): String {
        if (isBlank() || equals("None", ignoreCase = true)) return ""
        if (contains("ago", ignoreCase = true)) return this

        return try {
            val zdt = when {
                endsWith("Z") -> Instant.parse(this).atZone(ZoneId.systemDefault())
                contains("T") -> ZonedDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .withZoneSameInstant(ZoneId.systemDefault())
                length == 10 -> java.time.LocalDate.parse(this).atStartOfDay(ZoneId.systemDefault())
                length == 4 && all { it.isDigit() } -> return this
                else -> return this
            }

            zdt.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
        } catch (_: Exception) {
            this
        }
    }

    /**
     * Validates whether a string is a proper YouTube video URL.
     *
     * Supported formats:
     * - youtube.com/watch?v=VIDEO_ID
     * - youtu.be/VIDEO_ID
     *
     * @receiver URL string to validate.
     * @return true if valid YouTube video link, false otherwise.
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

    /**
     * Checks whether the string contains a valid YouTube playlist URL.
     *
     * @receiver URL string.
     * @return true if playlist parameter exists, false otherwise.
     */
    fun String.isValidYouTubePlaylistUrl(): Boolean {
        val regex = Regex(""".*?(youtube\.com|youtu\.be).*[?&]list=([a-zA-Z0-9_-]+)""")
        return regex.containsMatchIn(this)
    }

    /**
     * Extracts the playlist ID from a YouTube playlist URL.
     *
     * @param url Full playlist URL.
     * @return Playlist ID or null if not found.
     */
    fun extractPlaylistId(url: String): String? {
        val regex = Regex(""".*[?&]list=([a-zA-Z0-9_-]+)""")
        return regex.find(url)?.groupValues?.get(1)
    }

    /**
     * Loads the audio stream URL for a given [VideosListData] item.
     *
     * This is a suspend function and performs a network call internally.
     *
     * @param onSuccess Called when stream URL is successfully retrieved.
     * @param onFailure Called when an error occurs.
     */
    suspend fun VideosListData.loadStreamUrl(
        onSuccess: (ItemsStreamUrlsForMediaItemData) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            val result = py.getAudioStreamUrl(url)

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

    /**
     * Converts a Unix timestamp (milliseconds)
     * into format: dd/MM/yyyy
     *
     * @param timestamp Epoch time in milliseconds.
     * @return Formatted date string.
     */
    fun formatDateFromLong(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    /**
     * Converts raw view count into shortened format.
     *
     * Examples:
     * 1,200 → 1.2K
     * 1,200,000 → 1.2M
     * 1,200,000,000 → 1.2B
     *
     * @receiver Raw views string (may include commas or "views").
     * @return Shortened view count string.
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

    /**
     * Retrieves direct video stream URL.
     *
     * Suspend function — performs network call.
     *
     * @param videoId YouTube video ID.
     * @param onSuccess Called with direct stream URL.
     * @param onFailure Called if extraction fails.
     */
    suspend fun getVideoStreamUrl(videoId: String): String {
        val url = "https://www.youtube.com/watch?v=$videoId"
        val result = py.getVideoStreamUrl(url)

        if (result.success && !result.result.isNullOrEmpty()) {
            return result.result
        } else {
            throw PyCallError.PythonException(Throwable(result.error))
        }
    }

    /**
     * Retrieves direct audio stream URL.
     *
     * Suspend function — performs network call.
     *
     * @param mediaId YouTube video ID.
     */
    @Throws(PyCallError.PythonException::class)
    suspend fun getAudioStreamUrl(mediaId: String): String {
        val url = "https://www.youtube.com/watch?v=$mediaId"
        val result = py.getAudioStreamUrl(url)

        if (result.success && !result.result.isNullOrEmpty()) {
            return result.result
        } else {
            throw PyCallError.PythonException(Throwable(result.error))
        }
    }

    /**
     * Retrieves playlist metadata and video list.
     *
     * Suspend function — performs network call.
     *
     * @param playListUrl Full YouTube playlist URL.
     * @param onSuccess Returns playlist name and list of videos.
     * @param onFailure Called when retrieval fails.
     */
    suspend fun getPlayListStreamUrl(
        playListUrl: String,
        onSuccess: (playListName: String, videoList: List<PlayListDataClass>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val result = py.getPlaylistUrl(playListUrl)

            if (result.isNotEmpty()) {
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

    suspend fun search(query: String): ResponseVideo {
        val result = py.searchNow(query = query)
        return result
    }

    suspend fun searchByUrl(url: String): RespondVideoDetails {
        val result = py.searchByUrl(url = url)
        return result
    }

}
package com.das.python

import com.das.python.data.model.FewVideoDetails
import com.das.python.data.model.ItemsStreamUrlsForMediaItemData
import com.das.python.data.model.PlayListDataClass
import com.das.python.data.model.VideosListData
import com.das.python.data.model.responds.ApiResponse
import com.das.python.data.model.searcher.SearchResponse
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
 *
 * Example usage:
 * ```
 * val videoId = "https://www.youtube.com/watch?v=dQw4w9WgXcQ".youtubeExtractor()
 * if (videoId != null) {
 *     val streamUrl = YouTuber.getAudioStreamUrl(videoId)
 * }
 * ```
 */
object YouTuber {

    private val py = MainPyImpl()

    /**
     * Extracts a YouTube video ID from a given URL string.
     *
     * Supports standard, shortened, shorts, and embed YouTube links.
     *
     * Example:
     * ```
     * val id1 = "https://www.youtube.com/watch?v=dQw4w9WgXcQ".youtubeExtractor() // "dQw4w9WgXcQ"
     * val id2 = "https://youtu.be/dQw4w9WgXcQ".youtubeExtractor() // "dQw4w9WgXcQ"
     * val id3 = "https://www.youtube.com/shorts/dQw4w9WgXcQ".youtubeExtractor() // "dQw4w9WgXcQ"
     * ```
     *
     * @receiver Full YouTube URL.
     * @return The extracted video ID, or null if not found or invalid format.
     */
    fun String.youtubeExtractor(): String? {
        return try {
            val url = URL(trim())
            val host = url.host.lowercase()

            val videoId = when {
                host.contains("youtube.com") -> {
                    // Handles: watch?v=, shorts/, embed/
                    when {
                        url.path.startsWith("/watch") -> {
                            url.query
                                ?.split("&")
                                ?.mapNotNull {
                                    val parts = it.split("=")
                                    if (parts.size == 2) parts[0] to parts[1] else null
                                }
                                ?.toMap()
                                ?.get("v")
                        }

                        url.path.startsWith("/shorts/") -> {
                            url.path.removePrefix("/shorts/").substringBefore("?")
                        }

                        url.path.startsWith("/embed/") -> {
                            url.path.removePrefix("/embed/").substringBefore("?")
                        }

                        else -> null
                    }
                }

                host.contains("youtu.be") -> {
                    url.path.removePrefix("/").substringBefore("?")
                }

                else -> null
            }

            videoId?.takeIf { it.matches(Regex("^[A-Za-z0-9_-]{11}$")) }

        } catch (_: Exception) {
            null
        }
    }

    /**
     * Formats various ISO date formats into:
     * "dd MMM yyyy" (English locale).
     *
     * Supported inputs:
     * - ISO_INSTANT (e.g., "2023-10-27T10:00:00Z")
     * - ISO_OFFSET_DATE_TIME
     * - ISO_LOCAL_DATE (e.g., "2023-10-27")
     * - Year only (returns unchanged)
     *
     * Example:
     * ```
     * val date = "2023-10-27T10:00:00Z".formatDate() // "27 Oct 2023"
     * val year = "2023".formatDate() // "2023"
     * ```
     *
     * @receiver Raw date string.
     * @return Formatted date string or original string if parsing fails.
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
     * Example:
     * ```
     * val isValid = "https://www.youtube.com/watch?v=dQw4w9WgXcQ".isValidYoutubeURL() // true
     * ```
     *
     * @receiver URL string to validate.
     * @return true if valid YouTube video link, false otherwise.
     */
    fun String.isValidYoutubeURL(): Boolean {
        return try {
            val url = URL(trim())

            val host = url.host.lowercase()

            when {
                host.contains("youtube.com") -> {
                    val videoId = url.query
                        ?.split("&")
                        ?.mapNotNull {
                            val parts = it.split("=")
                            if (parts.size == 2) parts[0] to parts[1] else null
                        }
                        ?.toMap()
                        ?.get("v")

                    videoId?.matches(Regex("^[A-Za-z0-9_-]{11}$")) == true
                }

                host.contains("youtu.be") -> {
                    val videoId = url.path
                        .removePrefix("/")
                        .substringBefore("?")

                    videoId.matches(Regex("^[A-Za-z0-9_-]{11}$"))
                }

                else -> false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks whether the string contains a valid YouTube playlist URL.
     *
     * Example:
     * ```
     * val isPlaylist = "https://www.youtube.com/playlist?list=PLB01B73199D698063".isValidYouTubePlaylistUrl() // true
     * ```
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
     * Example:
     * ```
     * val id = YouTuber.extractPlaylistId("https://www.youtube.com/playlist?list=PLB01B73199D698063") // "PLB01B73199D698063"
     * ```
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
     * Example:
     * ```
     * videoData.loadStreamUrl(
     *     onSuccess = { streamData -> /* handle success */ },
     *     onFailure = { error -> /* handle error */ }
     * )
     * ```
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
     * Example:
     * ```
     * val date = 1698393600000L.toSimpleDate() // "27/10/2023"
     * ```
     *
     * @receiver Epoch time in milliseconds.
     * @return Formatted date string.
     */
    fun Long.toSimpleDate(): String {
        val date = Date(this)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    /**
     * Converts raw view count into shortened format (K, M, B).
     *
     * Examples:
     * ```
     * "1200".formatViews() // "1.2K"
     * "1200000".formatViews() // "1.2M"
     * "1,200,000,000 views".formatViews() // "1.2B"
     * ```
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
     * Converts a file size in bytes into a human-readable string.
     *
     * Example:
     * ```
     * val size = 1048576L.toHumanReadable() // "1 MB"
     * ```
     *
     * @receiver Size in bytes.
     * @return Formatted string with appropriate unit (B, KB, MB, GB, TB, PB).
     */
    fun Long.toHumanReadable(): String {
        if (this < 1024) return "$this B"

        val units = arrayOf("KB", "MB", "GB", "TB", "PB")
        var value = this.toDouble()
        var unitIndex = -1

        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024
            unitIndex++
        }

        val formatted = if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.ROOT, "%.2f", value)
                .trimEnd('0')
                .trimEnd('.')
        }

        return "$formatted ${units[unitIndex]}"
    }

    /**
     * Retrieves direct video stream URL.
     *
     * Suspend function — performs network call.
     *
     * Example:
     * ```
     * val url = YouTuber.getVideoStreamUrl("dQw4w9WgXcQ")
     * ```
     *
     * @param videoId YouTube video ID.
     * @return Direct stream URL.
     * @throws PyCallError.PythonException if extraction fails.
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
     * Example:
     * ```
     * val url = YouTuber.getAudioStreamUrl("dQw4w9WgXcQ")
     * ```
     *
     * @param mediaId YouTube video ID.
     * @return Direct audio stream URL.
     * @throws PyCallError.PythonException if extraction fails.
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
     * Example:
     * ```
     * YouTuber.getPlayListStreamUrl(
     *     playListUrl = "https://www.youtube.com/playlist?list=...",
     *     onSuccess = { name, videos -> /* handle success */ },
     *     onFailure = { error -> /* handle error */ }
     * )
     * ```
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

    /**
     * Searches for YouTube videos based on a query.
     *
     * Example:
     * ```
     * val response = YouTuber.search("Never Gonna Give You Up")
     * ```
     *
     * @param query Search keywords.
     * @return `ApiResponse<SearchResponse> ` containing search results.
     */
    suspend fun search(query: String): ApiResponse<SearchResponse> {
        val result = py.searchNow(query = query)
        return result
    }

    /**
     * Retrieves detailed video information using its URL.
     *
     * Example:
     * ```
     * val details = YouTuber.searchByUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
     * ```
     *
     * @param url Full YouTube video URL.
     * @return `ApiResponse<FewVideoDetails>` containing video metadata.
     */
    suspend fun searchByUrl(url: String): ApiResponse<FewVideoDetails> {
        val result = py.searchByUrl(url = url)
        return result
    }

}

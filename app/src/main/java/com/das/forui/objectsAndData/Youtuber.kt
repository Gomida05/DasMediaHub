package com.das.forui.objectsAndData

import android.os.Build
import android.util.Log
import com.chaquo.python.Python.getInstance
import com.das.forui.objectsAndData.ForUIKeyWords.YOUTUBE_HOST_1
import com.das.forui.objectsAndData.ForUIKeyWords.YOUTUBE_HOST_2
import com.das.forui.objectsAndData.ForUIKeyWords.YOUTUBE_HOST_3
import com.das.forui.objectsAndData.ForUIKeyWords.YOUTUBE_REGEX
import com.das.forui.objectsAndData.ForUIDataClass.PlayListDataClass
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern


object Youtuber {
    val pythonInstant = getInstance()




    /**
     * Extracts the YouTube video ID from a given URL using a predefined regex.
     *
     * @param url Full YouTube URL (e.g., "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
     * @return Video ID if found, or null if extraction fails.
     */
    fun youtubeExtractor(url: String): String? {

        val pattern = Regex(YOUTUBE_REGEX)
        val match = pattern.find((url))
        return match?.groups?.get(1)?.value
    }

    /**
     * Returns data format like this dd/MMM/yyyy ENGLISH
     */
    fun formatDate(dateStr: String): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                val zonedDateTime = ZonedDateTime.parse(dateStr, inputFormatter)
                val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

                zonedDateTime.format(outputFormatter)
            } else {
                val inputFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH)
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

                outputFormat.format(inputFormat.parse(dateStr) ?: return dateStr)
            }
        } catch (e: Exception) {
            println("Found an error right here: ${e.message}")
            dateStr
        }
    }

    /**
     * Validates if the provided URL is a valid YouTube video link.
     *
     * Supports:
     * - Standard YouTube URLs (youtube.com/watch?v=...)
     * - Shortened URLs (youtu.be/VIDEO_ID)
     *
     * @param youTubeUrl URL to validate
     * @return true if the URL is a valid YouTube video link, false otherwise.
     */
    fun isValidYoutubeURL(youTubeUrl: String): Boolean {
        try {
            val trimmedUrl = youTubeUrl.trim()
            val cleanedUrl =
                if (trimmedUrl.endsWith("&feature=shared"))
                    trimmedUrl.removeSuffix("&feature=shared") else trimmedUrl

            val url = URL(cleanedUrl)

            val host = url.host
            if (host == YOUTUBE_HOST_1 || host == YOUTUBE_HOST_2) {
                val videoPattern = Pattern.compile("^/watch\\?v=([A-Za-z0-9_-]{11})$")
                val matcher = videoPattern.matcher("${url.path}?${url.query}")
                return matcher.matches()
            } else if (host == YOUTUBE_HOST_3) {
                // Shortened YouTube URL (youtu.be/VIDEO_ID)
                val videoPattern = Pattern.compile("^/([A-Za-z0-9_-]{11})$")
                val matcher = videoPattern.matcher(url.path)  // Check the path only
                return matcher.matches()
            }

            return false
        } catch (e: Exception) {
            println("error on isValidYoutubeUrl: ${e.message}")

            return false
        }
    }

    fun isValidYouTubePlaylistUrl(url: String): Boolean {
        val regex = Regex(""".*?(youtube\.com|youtu\.be).*[?&]list=([a-zA-Z0-9_-]+)""")
        return regex.containsMatchIn(url)
    }

    fun extractPlaylistId(url: String): String? {
        val regex = Regex(""".*[?&]list=([a-zA-Z0-9_-]+)""")
        return regex.find(url)?.groupValues?.get(1)
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
     * @param views Number of views
     * @return Formatted string with K, M, or B suffix
     */
    fun formatViews(views: Long): String {
        return when {
            views >= 1_000_000_000 -> "%.1fB".format(views / 1_000_000_000.0)
            views >= 1_000_000 -> "%.1fM".format(views / 1_000_000.0)
            views >= 1_000 -> "%.1fK".format(views / 1_000.0)
            else -> views.toString()
        }
    }


    /**
     * Extension function to convert a duration in milliseconds (Long) to a formatted time string.
     *
     * Formats adaptively based on the length of the duration:
     * - If duration is 0 or negative, returns "00:00"
     * - If duration includes days, format is "dd:hh:mm:ss"
     * - If duration includes hours, format is "hh:mm:ss"
     * - If duration includes minutes, format is "mm:ss"
     * - If duration is less than a minute, format is "ss.ms"
     */
    fun Long.formatTimeFromMs(): String {
        if (this <= 0L) {
            return "00:00"
        }
        val totalSeconds = this / 1000
        val milliseconds = this % 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = (totalSeconds / 3600) % 24
        val days = totalSeconds / (3600 * 24)

        return when {
            days > 0 -> String.format(
                Locale.ENGLISH,
                "%02d:%02d:%02d:%02d",
                days,
                hours,
                minutes,
                seconds
            )

            hours > 0 -> String.format(
                Locale.ENGLISH,
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )

            minutes > 0 -> String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
            else -> String.format(Locale.ENGLISH, "00:%02d.%03d", seconds, milliseconds)
        }
    }


    fun getVideoStreamUrl(
        videoId: String,
        onSuccess: (streamUrl: String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val mainFile = pythonInstant.getModule("main")
                val variable = mainFile["get_video_url"]


                val result = variable?.call("https://www.youtube.com/watch?v=${videoId}").toString()

                if (result != "False") {
                    withContext(Dispatchers.Main) {
                        onSuccess(result)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Something went wrong with result: $result")
                    }
                }
            }
        } catch (e: Exception) {
            onFailure("Something went wrong with result: ${e.message}")
        }

    }
    fun getAudioStreamUrl(
        videoId: String,
        onSuccess: (streamUrl: String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val mainFile = pythonInstant.getModule("main")
                val variable = mainFile["get_audio_url"]


                val result = variable?.call("https://www.youtube.com/watch?v=${videoId}").toString()

                if (result != "False") {
                    // Switch to the main thread for UI updates
                    withContext(Dispatchers.Main) {
                        onSuccess(result)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Something went wrong with result: $result")
                    }
                }
            }
        } catch (e: Exception) {
            onFailure("Something went wrong with result: ${e.message}")
        }

    }

    fun getPlayListStreamUrl(
        playListUrl: String,
        onSuccess: (playListName: String, videoList: List<PlayListDataClass>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            CoroutineScope(Dispatchers.IO).launch {


                val result = callPythonSearchSuggestion(playListUrl)

                if (!result.isNullOrEmpty()) {
                    // Switch to the main thread for UI updates
                    withContext(Dispatchers.Main) {
                        onSuccess(
                            "Testing PLayList Downloader",
                            result
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Something went wrong with result: $result")
                    }
                }
            }
        } catch (e: Exception) {
            onFailure("Something went wrong with result: ${e.message}")
        }
    }


    private fun callPythonSearchSuggestion(inputText: String): List<PlayListDataClass>? {
        return try {

            val mainFile = pythonInstant.getModule("main")
            val getResultFromPython = mainFile["getPlayListUrls"]?.call(inputText)

            if (!getResultFromPython.isNullOrEmpty()) {
                val videosListDataListType = object : TypeToken<List<PlayListDataClass>>() {}.type

                val result: List<PlayListDataClass>? = Gson().fromJson(getResultFromPython.toString(), videosListDataListType)
                Log.e("Python Playlist Result", "yes result is here")
                result
            }
            else{
                null
            }
        } catch (e: JsonSyntaxException) {
            Log.e("JSON Error", "Error parsing JSON: ${e.message}")
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

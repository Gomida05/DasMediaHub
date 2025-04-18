@file:Suppress("BadConfigurationProvider")
package com.das.forui

import android.app.Application
import android.os.Build
import androidx.work.Configuration
import com.chaquo.python.Python
import com.chaquo.python.Python.getInstance
import com.chaquo.python.android.AndroidPlatform
import com.das.forui.MainApplication.Youtuber.pythonInstant
import com.das.forui.objectsAndData.ForUIKeyWords.YOUTUBE_HOST_1
import com.das.forui.objectsAndData.ForUIKeyWords.YOUTUBE_HOST_2
import com.das.forui.objectsAndData.ForUIKeyWords.YOUTUBE_HOST_3
import com.das.forui.objectsAndData.ForUIKeyWords.YOUTUBE_REGEX
import com.das.forui.objectsAndData.ForUIDataClass.ItemsStreamUrlsForMediaItemData
import com.das.forui.objectsAndData.ForUIDataClass.VideosListData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

class MainApplication: Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .build()
    override fun onCreate() {
        super.onCreate()


        CoroutineScope(Dispatchers.IO).launch {

            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this@MainApplication))
            }
        }.start()
    }


    fun getListItemsStreamUrls(
        data: VideosListData,
        onSuccess: (ItemsStreamUrlsForMediaItemData) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val mainFile = pythonInstant.getModule("main")
                val variable = mainFile["get_audio_url"]
                var details: ItemsStreamUrlsForMediaItemData?

                val result =
                    variable?.call("https://www.youtube.com/watch?v=${data.videoId}").toString()

                if (result != "False") {
                    // Switch to the main thread for UI updates
                    withContext(Dispatchers.Main) {
                        details = ItemsStreamUrlsForMediaItemData(
                            result,
                            data.videoId,
                            data.title,
                            data.views,
                            data.dateOfVideo,
                            data.duration,
                            data.channelName,
                            data.channelThumbnailsUrl
                        )
                        // Call the success callback
                        details?.let { onSuccess(it) }
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
                        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH)
                    val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

                    outputFormat.format(inputFormat.parse(dateStr) ?: return dateStr)
                }
            } catch (e: Exception) {
                println("Found an error right here: ${e.message}")
                dateStr
            }
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
    }
}
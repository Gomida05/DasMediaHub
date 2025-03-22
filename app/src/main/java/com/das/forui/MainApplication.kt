package com.das.forui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.chaquo.python.Python
import com.chaquo.python.Python.getInstance
import com.chaquo.python.android.AndroidPlatform
import com.das.forui.MainApplication.Youtuber.pythonInstant
import com.das.forui.objectsAndData.ItemsStreamUrlsForMediaItemData
import com.das.forui.objectsAndData.VideosListData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Thread {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
        }.start()

        val sharedPref: SharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val currentUiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val uiModeType = sharedPref.getInt("isNightModeOn", currentUiMode)
        AppCompatDelegate.setDefaultNightMode(uiModeType)
    }




    fun getListItemsStreamUrls(
        data: VideosListData,
        onSuccess: (ItemsStreamUrlsForMediaItemData) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val mainFile = pythonInstant.getModule("main")
            val variable = mainFile["get_audio_url"]
            var details: ItemsStreamUrlsForMediaItemData?

            val result = variable?.call("https://www.youtube.com/watch?v=${data.videoId}").toString()

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
    }



    object Youtuber {
        val pythonInstant = getInstance()
        fun extractor(url: String): String? {
            val regex = "(?<=v=|/)([a-zA-Z0-9_-]{11})(?=&|\$|/)"
            val pattern = Regex(regex)
            val match = pattern.find((url))
            return match?.groups?.get(1)?.value
        }

        fun isValidYoutubeURL(youTubeUrl: String): Boolean {
            try {
                val trimmedUrl = youTubeUrl.trim()
                val cleanedUrl =
                    if (trimmedUrl.endsWith("&feature=shared")) trimmedUrl.removeSuffix("&feature=shared") else trimmedUrl

                val url = URL(cleanedUrl)

                val host = url.host
                if (host == "www.youtube.com" || host == "youtube.com") {
                    val videoPattern = Pattern.compile("^/watch\\?v=([A-Za-z0-9_-]{11})$")
                    val matcher =
                        videoPattern.matcher(url.path + "?" + url.query)  // Combine path and query
                    return matcher.matches()
                } else if (host == "youtu.be") {
                    // Shortened YouTube URL (youtu.be/VIDEO_ID)
                    val videoPattern = Pattern.compile("^/([A-Za-z0-9_-]{11})$")
                    val matcher = videoPattern.matcher(url.path)  // Check the path only
                    return matcher.matches()
                }

                // If not youtube.com or youtu.be, return false
                return false
            } catch (e: Exception) {
                println("yes with me3 ${e.message}")
//            alertUserError(e.message)
                return false
            }
        }

        /**
         * Returns data format like this dd/MMM/yyyy ENGLISH
         */
        fun formatDate(dateStr: String): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

                val zonedDateTime = ZonedDateTime.parse(dateStr, inputFormatter)

                val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

                return zonedDateTime.format(outputFormatter)
            }
            else{
                return dateStr
            }
        }

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
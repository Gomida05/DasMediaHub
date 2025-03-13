package com.das.forui

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.das.forui.MainActivity.Youtuber.pythonInstant
import com.das.forui.ui.viewer.ViewerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Thread {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
        }.start()
    }




    fun getListItemsStreamUrls(
        data: ViewerFragment.Video,
        onSuccess: (MediaItemDetails) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val mainFile = pythonInstant.getModule("main")
            val variable = mainFile["get_audio_url"]
            var details: MediaItemDetails?

            val result = variable?.call("https://www.youtube.com/watch?v=${data.videoId}").toString()

            if (result != "False") {
                // Switch to the main thread for UI updates
                withContext(Dispatchers.Main) {
                    details = MediaItemDetails(
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
                // Switch to the main thread for UI updates
                withContext(Dispatchers.Main) {
                    onFailure("Something went wrong with result: $result")
                }
            }
        }
    }

    data class MediaItemDetails(
        val audioUrl: String,
        val videoId: String,
        val title: String,
        val views: String,
        val dateOfVideo: String,
        val duration: String,
        val channelName: String,
        val channelThumbnailsUrl: String
    )
}
package com.das.forui

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.das.forui.MainActivity.Youtuber.pythonInstant
import com.das.forui.objectsAndData.ItemsStreamUrlsForMediaItemData
import com.das.forui.objectsAndData.VideosListData
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
                // Switch to the main thread for UI updates
                withContext(Dispatchers.Main) {
                    onFailure("Something went wrong with result: $result")
                }
            }
        }
    }
}
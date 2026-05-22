package com.das.mediaHub

import android.app.Application
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.das.python.PythonMain.startPython
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {

    private val notificationChannels by lazy {
        NotificationChannels(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        startPython()
        notificationChannels.createAllNotificationChannels()

    }

}

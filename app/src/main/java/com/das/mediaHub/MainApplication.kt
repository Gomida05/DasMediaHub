package com.das.mediaHub

import android.app.Application
import com.das.python.PythonMain.startPython

class MainApplication: Application() {

    private val notificationChannels by lazy {
        NotificationChannels(this)
    }

    override fun onCreate() {
        super.onCreate()
        startPython()
        notificationChannels.createAllNotificationChannels()
    }
}
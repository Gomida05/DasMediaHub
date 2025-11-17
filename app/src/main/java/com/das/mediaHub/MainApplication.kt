package com.das.mediaHub

import android.app.Application
import com.das.mediaHub.python.PythonMain.startPython

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startPython()
        NotificationChannels(this).createAllNotificationChannels()
    }
}
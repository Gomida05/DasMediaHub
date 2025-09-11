package com.das.mediaHub

import android.app.Application
import android.os.Build
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if (!Python.isStarted()){
            Python.start(AndroidPlatform(this))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannels(this).createAllNotificationChannels()
        }
    }
}
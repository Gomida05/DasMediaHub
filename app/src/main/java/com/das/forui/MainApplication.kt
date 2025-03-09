package com.das.forui

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Thread {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
        }.start()
    }




}
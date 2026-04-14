package com.das.mediaHub

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.das.mediaHub.data.local.AppDatabase
import com.das.mediaHub.data.local.db.FavoritesDatabase
import com.das.mediaHub.data.local.db.SearchDatabase
import com.das.mediaHub.data.mediacontroller.online.VideoPlayerListener
import com.das.mediaHub.data.mediacontroller.online.VideoPlayerManager
import com.das.python.PythonMain.startPython

internal class MainApplication: Application() {

    lateinit var videoPlayerMainApplication: VideoPlayerManager

    private val notificationChannels by lazy {
        NotificationChannels(this)
    }

    override fun onCreate() {
        super.onCreate()
        startPython()
        notificationChannels.createAllNotificationChannels()
        videoPlayerMainApplication = VideoPlayerManager(applicationContext)

    }
    val appDatabase: AppDatabase by lazy {
        AppDatabase(applicationContext)
    }


}

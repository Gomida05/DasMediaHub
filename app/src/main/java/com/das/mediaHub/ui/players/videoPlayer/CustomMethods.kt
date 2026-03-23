package com.das.mediaHub.ui.players.videoPlayer

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object CustomMethods {

    private fun Activity.setFullscreen(fullscreen: Boolean) {

        WindowCompat.setDecorFitsSystemWindows(window, !fullscreen)
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        if (fullscreen) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }


    fun Activity.rotateScreen(fullScreen: Boolean) {
        requestedOrientation = if (fullScreen) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        setFullscreen(fullScreen || isInPictureInPictureMode)
    }





}
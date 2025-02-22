package com.das.forui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MediaControlFullScreenBroadcast: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("ControlMedia")

        when (action) {
            "play" -> {
                // Call your media play method
                playMedia(context)
            }

            "pause" -> {
                // Call your media pause method
                pauseMedia(context)
            }
            "cancel" ->{

            }
        }
    }

    private fun playMedia(context: Context) {
    }

    private fun pauseMedia(context: Context) {
    }
}
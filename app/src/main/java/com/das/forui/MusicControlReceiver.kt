package com.das.forui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MusicControlReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PLAY -> {
                // Start or resume playback
                startPlayback(context)
            }
            ACTION_PAUSE -> {
                // Pause playback
                pausePlayback(context)
            }
            ACTION_STOP -> {
                // Stop playback and cleanup
                stopPlayback(context)
            }
        }
    }

    private fun startPlayback(context: Context) {
        val serviceIntent = Intent(context, AudioServiceFromUrl::class.java)
        serviceIntent.action = ACTION_PLAY
        context.startService(serviceIntent)
    }

    private fun pausePlayback(context: Context) {
        val serviceIntent = Intent(context, AudioServiceFromUrl::class.java)
        serviceIntent.action = ACTION_PAUSE
        context.startService(serviceIntent)
    }

    private fun stopPlayback(context: Context) {
        val serviceIntent = Intent(context, AudioServiceFromUrl::class.java)
        serviceIntent.action = ACTION_STOP
        context.startService(serviceIntent)
    }

    companion object{
        const val ACTION_PLAY = "com.das.forui.ACTION_PLAY"
        const val ACTION_PAUSE = "com.das.forui.ACTION_PAUSE"
        const val ACTION_STOP = "com.das.forui.ACTION_STOP"
        const val ACTION_START= "com.das.forui.Start"
        const val ACTION_PREVIOUS = "com.das.forui.PLAY"
        const val ACTION_PAUSE_PLAY = "com.das.forui.PAUSE"
        const val ACTION_NEXT = "com.das.forui.STOP"
        const val ACTION_KILL= "com.das.forui.kill"
        const val ACTION_ADD_TO_WATCH_LATER ="com.das.forui.ACTION_ADD_TO_WATCH_LATER"
        const val ACTION_DELETE_INTENT = "com.das.forui.DELETE_INTENT"
    }
}
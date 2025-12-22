package com.das.mediaHub

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService


internal class NotificationChannels(context: Context) {

    private val manager = context.getSystemService<NotificationManager>()

    fun createAllNotificationChannels() {
        manager?.let {
            it.createChannelGroups()
            it.createChannels()
        }
    }

    private fun NotificationManager.createChannelGroups() {
        val groups = listOf(
            NotificationChannelGroup("MNGC", "MediaPlayer notifications"),
            NotificationChannelGroup("NGC", "Download notifications")
        )
        createNotificationChannelGroups(groups)
    }

    private fun NotificationManager.createChannels() {
        val channels = listOf(
            NotificationChannel(
                "error_searching",
                "Error Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for error notifications"
                enableVibration(true)
            },
            NotificationChannel(
                "download_channel",
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                group = "NGC"
                description = "This channel is for download notifications"
            },
            NotificationChannel(
                "MediaYouTubePlayer",
                "Media Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                group = "MNGC"
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
        )

        createNotificationChannels(channels)
    }
}

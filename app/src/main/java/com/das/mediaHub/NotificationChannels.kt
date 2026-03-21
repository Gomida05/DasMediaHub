package com.das.mediaHub

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import com.das.mediaHub.data.constants.Notifications.DOWNLOADER_NOTIFICATION_CHANNEL


internal class NotificationChannels(context: Context) {

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createAllNotificationChannels() {
        createChannelGroups()
        createChannels()
    }

    private fun createChannelGroups() {
        val groups = listOf(
            NotificationChannelGroup("MNGC", "MediaPlayer notifications"),
            NotificationChannelGroup("NGC", "Download notifications")
        )
        manager.createNotificationChannelGroups(groups)
    }

    private fun createChannels() {
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
            },
            NotificationChannel(
                "MusicPlayerNotification",
                "Local Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                group = "MNGC"
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            },
            NotificationChannel(
                DOWNLOADER_NOTIFICATION_CHANNEL,
                "Media Downloader",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableVibration(false)
                setShowBadge(false)
                description = "Media Downloader for videos and music"
                enableLights(false)
                setSound(null, null)
            }
        )

        manager.createNotificationChannels(channels)
    }
}

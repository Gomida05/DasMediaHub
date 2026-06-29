package com.das.mediaHub

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import com.das.downloader.DownloadNotifier.Companion.DOWNLOADER_NOTIFICATION_CHANNEL

internal class NotificationChannels(context: Context) {

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createAllNotificationChannels() {
        createChannelGroups()
        createChannels()
    }

    private fun createChannelGroups() {
        val groups = listOf(
            NotificationChannelGroup(MEDIA_GROUP, "MediaPlayer notifications"),
            NotificationChannelGroup(DOWNLOAD_GROUP, "Download notifications")
        )
        manager.createNotificationChannelGroups(groups)
    }

    private fun createChannels() {
        val channels = listOf(
            NotificationChannel(
                ERROR_SEARCHING,
                "Error Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for error notifications"
                enableVibration(true)
            },
            NotificationChannel(
                DOWNLOAD_CHANNEL,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                group = DOWNLOAD_GROUP
                description = "This channel is for download notifications"
            },
            NotificationChannel(
                MEDIA_YOUTUBE_PLAYER,
                "Media Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                group = MEDIA_GROUP
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            },
            NotificationChannel(
                MUSIC_PLAYER_NOTIFICATION,
                "Local Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                group = MEDIA_GROUP
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            },
            NotificationChannel(
                DOWNLOADER_NOTIFICATION_CHANNEL,
                "Media Downloader",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                group = DOWNLOAD_GROUP
                enableVibration(false)
                setShowBadge(false)
                description = "Media Downloader for videos and music"
                enableLights(false)
                setSound(null, null)
            }
        )

        manager.createNotificationChannels(channels)
    }


    companion object NotificationChannelNames {
        const val ERROR_SEARCHING = "error_searching"
        const val DOWNLOAD_CHANNEL = "download_channel"
        const val MEDIA_YOUTUBE_PLAYER = "MediaYouTubePlayer"
        const val MUSIC_PLAYER_NOTIFICATION = "MusicPlayerNotification"
        const val MEDIA_GROUP = "MNGC"
        const val DOWNLOAD_GROUP = "NGC"
    }
}

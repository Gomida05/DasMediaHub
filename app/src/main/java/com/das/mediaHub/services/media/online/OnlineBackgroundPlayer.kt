package com.das.mediaHub.services.media.online

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.BitmapLoader
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.das.mediaHub.MainActivity
import com.das.mediaHub.R
import com.das.mediaHub.Receiver
import com.das.mediaHub.data.constants.Action
import com.das.mediaHub.data.constants.Notifications
import com.das.mediaHub.data.constants.Notifications.OPEN_IT_NOW
import com.das.mediaHub.data.local.db.dao.FavoritesDao
import com.das.mediaHub.data.mediacontroller.online.MediaSessionPlaybackState
import com.das.mediaHub.data.model.PlaybackPayload
import com.das.python.data.model.ItemsStreamUrlsForMediaItemData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A service for handling background audio playback from online sources (e.g., YouTube).
 *
 * It uses [ExoPlayer] to stream media and integrates with [MediaSession] to provide
 * system-wide playback controls. It features custom [BitmapLoader] using Glide for
 * loading album art from URLs.
 *
 * Example usage:
 * ```kotlin
 * // Using the extension function provided in the companion object
 * context.playAudioFromUrl(audioUrl, selectedMediaItem)
 * ```
 */
@AndroidEntryPoint
class OnlineBackgroundPlayer : MediaSessionService() {

    @Inject
    lateinit var favoritesDao: FavoritesDao

    private var currentPayload: PlaybackPayload? = null

    @Inject
    lateinit var exoPlayer: ExoPlayer

    //For now
    private val mediaSession by lazy {
        @SuppressLint("UnsafeOptInUsageError", "RestrictedApi")
        val session = MediaSession.Builder(applicationContext, exoPlayer)
            .setSessionActivity(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    Intent(applicationContext, MainActivity::class.java),
                    FLAGS
                )
            )
            .build()
        session
    }

    private val notificationManager by lazy {
        getSystemService<NotificationManager>()
    }

    @Inject
    lateinit var mediaSessionState: MediaSessionPlaybackState



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            Action.ACTION_KILL -> {
                stopSelf()
                return START_NOT_STICKY
            }

            Action.ACTION_START -> {

                val payload = intent.toPlaybackPayload()
                if (currentPayload?.videoId == payload?.videoId && exoPlayer.isPlaying) {
                    moveToForeground()
                    return START_STICKY
                }
                if (payload == null) {
                    Log.e(TAG, "Invalid playback payload")
                    stopSelf()
                    return START_NOT_STICKY
                }

                currentPayload = payload
                startPlayback(payload)
                moveToForeground()
                return START_STICKY
            }
        }

        return START_NOT_STICKY
    }

    private fun moveToForeground() {
        val notification = createMediaNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startPlayback(payload: PlaybackPayload) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(payload.videoId)
            .setUri(payload.mediaUrl.toUri())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .setDisplayTitle(payload.title)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .setDescription(payload.channelName)
                    .setAlbumArtist("Unknown Album")
                    .setDurationMs(payload.duration.toLongOrNull() ?: 0L)
                    .setArtworkUri("https://img.youtube.com/vi/${payload.videoId}/0.jpg".toUri())
                    .build()
            )
            .build()

        exoPlayer.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }

    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun createMediaNotification(): Notification {
        val deleteIntent = Intent(this, Receiver::class.java).apply {
            action = Notifications.AUDIO_SERVICE_FROM_URL_NOTIFICATION
        }

        val deletePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            deleteIntent,
            FLAGS
        )
        val mainIntent = Intent(
            applicationContext,
            MainActivity::class.java
        ).apply {
            action = OPEN_IT_NOW
            putExtra("VideoID", currentPayload?.videoId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            FLAGS
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.music_note_24dp)
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(mediaSession)
            )
            .setDeleteIntent(deletePendingIntent)
            .setSettingsText("DasMediaHub Media Player")
            .build()
    }

    private fun updateNotification() {
        notificationManager?.notify(NOTIFICATION_ID, createMediaNotification())
    }

    override fun onGetSession(p0: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    private fun Intent.toPlaybackPayload(): PlaybackPayload? {
        val videoId = getStringExtra(EXTRA_VIDEO_ID).orEmpty()
        val mediaUrl = getStringExtra(EXTRA_MEDIA_URL).orEmpty()

        if (videoId.isBlank() || mediaUrl.isBlank()) return null

        return PlaybackPayload(
            videoId = videoId,
            mediaUrl = mediaUrl,
            title = getStringExtra(EXTRA_TITLE).orEmpty(),
            channelName = getStringExtra(EXTRA_CHANNEL_NAME).orEmpty(),
            views = getStringExtra(EXTRA_VIEWS).orEmpty(),
            date = getStringExtra(EXTRA_DATE).orEmpty(),
            duration = getStringExtra(EXTRA_DURATION).orEmpty()
        )
    }

    override fun onDestroy() {
        exoPlayer.release()
        mediaSession .release()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        const val TAG = "OnlineBackgroundPlayer"
        private const val CHANNEL_ID = "MediaYouTubePlayer"
        private const val NOTIFICATION_ID = 25

        const val EXTRA_VIDEO_ID = "extra_video_id"
        const val EXTRA_MEDIA_URL = "extra_media_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CHANNEL_NAME = "extra_channel_name"
        const val EXTRA_VIEWS = "extra_views"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_DURATION = "extra_duration"


        fun Context.playAudioFromUrl(
            audioUrl: String,
            selectedItem: ItemsStreamUrlsForMediaItemData
        ) {
            startAudioService(
                videoId = selectedItem.videoId,
                audioUrl = audioUrl,
                title = selectedItem.title,
                channelName = selectedItem.channelName,
                views = selectedItem.views,
                date = selectedItem.dateOfVideo,
                duration = selectedItem.duration
            )
        }

        private fun Context.startAudioService(
            videoId: String,
            audioUrl: String,
            title: String?,
            channelName: String?,
            views: String?,
            date: String?,
            duration: String?
        ) {

            val playIntent = Intent(this, OnlineBackgroundPlayer::class.java).apply {
                action = Action.ACTION_START
                putExtra(EXTRA_VIDEO_ID, videoId)
                putExtra(EXTRA_MEDIA_URL, audioUrl)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_CHANNEL_NAME, channelName)
                putExtra(EXTRA_VIEWS, views)
                putExtra(EXTRA_DATE, date)
                putExtra(EXTRA_DURATION, duration)
            }
            startService(playIntent)
        }

        private const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
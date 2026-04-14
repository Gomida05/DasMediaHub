package com.das.mediaHub.services.media

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.das.mediaHub.MainActivity
import com.das.mediaHub.MainApplication
import com.das.mediaHub.R
import com.das.mediaHub.Receiver
import com.das.mediaHub.data.constants.Action
import com.das.mediaHub.data.constants.Action.ACTION_KILL
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.constants.Notifications
import com.das.mediaHub.data.mediacontroller.online.MediaSessionPlaybackState
import com.das.mediaHub.data.model.state.VideoUiState
import com.das.mediaHub.data.repository.FavoritesRepository
import com.das.python.data.model.ItemsStreamUrlsForMediaItemData
import com.das.python.data.model.VideosListData

class OnlineBackgroundPlayer : Service() {

    private var currentPayload: PlaybackPayload? = null

    private val exoPlayer by lazy {
        ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    private val notificationManager by lazy {
        getSystemService<NotificationManager>()
    }

    private val favoritesDao by lazy {
        (this.applicationContext as MainApplication)
            .appDatabase
            .favoritesDatabase
            .favoritesDao()
    }

    private val db by lazy {
        FavoritesRepository(favoritesDao)
    }

    private val mediaSessionState by lazy {
        MediaSessionPlaybackState(db)
    }

    private val myMediaSession by lazy {
        MediaSessionCompat(this, TAG).apply {
            isActive = true
            setMediaButtonReceiver(
                PendingIntent.getBroadcast(
                    this@OnlineBackgroundPlayer,
                    0,
                    Intent(Intent.ACTION_MEDIA_BUTTON),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }

    private val playerListener = object : Player.Listener {

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            val payload = currentPayload ?: return
            myMediaSession.setPlaybackState(
                mediaSessionState.setStateToPlaying(newPosition.positionMs, payload.videoId)
            )
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val payload = currentPayload ?: return
            val currentPosition = exoPlayer.currentPosition

            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    myMediaSession.apply {
                        setPlaybackState(
                            mediaSessionState.setStateToLoading(currentPosition, payload.videoId)
                        )
                        isActive = true
                    }
                }

                Player.STATE_ENDED -> {
                    myMediaSession.apply {
                        setPlaybackState(
                            mediaSessionState.setStateToPaused(currentPosition, payload.videoId)
                        )
                        isActive = false
                    }
                }

                Player.STATE_IDLE -> {

                }

                Player.STATE_READY -> {

                }
            }
            updateNotification()
        }

        override fun onPlayerError(error: PlaybackException) {
            val payload = currentPayload ?: return
            Log.e(TAG, "Player error", error)
            myMediaSession.setPlaybackState(
                mediaSessionState.setStateToPaused(exoPlayer.currentPosition, payload.videoId)
            )
            updateNotification()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val payload = currentPayload ?: return
            val currentPosition = exoPlayer.currentPosition
            val currentDuration = exoPlayer.duration

            myMediaSession.apply {
                setPlaybackState(
                    when {
                        isPlaying -> mediaSessionState.setStateToPlaying(currentPosition, payload.videoId)
                        exoPlayer.isLoading -> mediaSessionState.setStateToLoading(currentPosition, payload.videoId)
                        else -> mediaSessionState.setStateToPaused(currentPosition, payload.videoId)
                    }
                )
            }

            updateMetadata(
                title = payload.title,
                channelName = payload.channelName,
                duration = currentDuration
            )
            updateNotification()
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            val payload = currentPayload ?: return
            val currentPosition = exoPlayer.currentPosition
            val currentDuration = exoPlayer.duration

            if (!isLoading) {
                myMediaSession.setPlaybackState(
                    mediaSessionState.setStateToPlaying(currentPosition, payload.videoId)
                )
                updateMetadata(
                    title = payload.title,
                    channelName = payload.channelName,
                    duration = currentDuration
                )
            } else {
                myMediaSession.setPlaybackState(
                    mediaSessionState.setStateToLoading(currentPosition, payload.videoId)
                )
            }

            updateNotification()
        }
    }

    private inner class MyMediaSessionCallBack : MediaSessionCompat.Callback() {

        override fun onSeekTo(pos: Long) {
            exoPlayer.seekTo(pos)
        }

        override fun onPlay() {
            exoPlayer.play()
        }

        override fun onPause() {
            exoPlayer.pause()
        }

        override fun onSkipToPrevious() {
            exoPlayer.seekToPrevious()
        }

        override fun onSkipToNext() {
            exoPlayer.seekToNext()
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            val payload = currentPayload ?: return

            when (action) {
                Action.ACTION_ADD_TO_WATCH_LATER -> {
                    val mediaDetails = VideosListData(
                        payload.videoId,
                        payload.title,
                        payload.date,
                        payload.views,
                        payload.duration,
                        payload.channelName,
                        ""
                    )

                    myMediaSession.setPlaybackState(
                        mediaSessionState.addItOrRemoveFromDB(
                            exoPlayer.currentPosition,
                            mediaDetails
                        )
                    )
                    updateNotification()
                }

                ACTION_KILL -> stopSelf()
            }
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            if (mediaButtonEvent == null) return super.onMediaButtonEvent(mediaButtonEvent)

            val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            } else {
                @Suppress("DEPRECATION")
                mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            }

            if (mediaButtonEvent.action == Intent.ACTION_MEDIA_BUTTON && keyEvent != null) {
                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> exoPlayer.pause()
                    KeyEvent.KEYCODE_MEDIA_PLAY -> exoPlayer.play()
                    KeyEvent.KEYCODE_MEDIA_NEXT -> onSkipToNext()
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> onSkipToPrevious()
                }
                return true
            }

            return super.onMediaButtonEvent(mediaButtonEvent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        exoPlayer.addListener(playerListener)
        myMediaSession.setCallback(MyMediaSessionCallBack())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_KILL -> {
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_START -> {
                val payload = intent.toPlaybackPayload()
                if (payload == null) {
                    Log.e(TAG, "Invalid playback payload")
                    stopSelf()
                    return START_NOT_STICKY
                }

                currentPayload = payload
                startPlayback(payload)
                MediaButtonReceiver.handleIntent(myMediaSession, intent)
                startForeground(NOTIFICATION_ID, createMediaNotification())
                return START_STICKY
            }
        }

        return START_NOT_STICKY
    }

    private fun startPlayback(payload: PlaybackPayload) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(payload.videoId)
            .setUri(payload.mediaUrl.toUri())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .setDisplayTitle(payload.title)
                    .build()
            )
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        myMediaSession.setPlaybackState(
            mediaSessionState.setStateToLoading(exoPlayer.currentPosition, payload.videoId)
        )

        updateMetadata(
            title = payload.title,
            channelName = payload.channelName,
            duration = exoPlayer.duration
        )
    }

    private fun updateMetadata(title: String, channelName: String, duration: Long) {
        val payload = currentPayload ?: return
        val defaultBitmap = BitmapFactory.decodeResource(resources, R.drawable.music_note_24dp)

        val baseBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, payload.mediaUrl)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, channelName)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "unknown album")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, defaultBitmap)

        myMediaSession.setMetadata(baseBuilder.build())

        Glide.with(this)
            .asBitmap()
            .load("https://img.youtube.com/vi/${payload.videoId}/0.jpg")
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val updated = MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, payload.mediaUrl)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, channelName)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "unknown album")
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource)
                        .build()

                    myMediaSession.setMetadata(updated)
                    updateNotification()
                }

                override fun onLoadCleared(placeholder: Drawable?) = Unit
            })
    }

    private fun createMediaNotification(): Notification {
        val deleteIntent = Intent(this, Receiver::class.java).apply {
            action = Notifications.AUDIO_SERVICE_FROM_URL_NOTIFICATION
        }

        val deletePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.music_note_24dp)
            .setStyle(
                NotificationCompat.MediaStyle()
                    .setMediaSession(myMediaSession.sessionToken)
            )
            .setDeleteIntent(deletePendingIntent)
            .setSettingsText("DasMediaHub Media Player")
            .build()
    }

    private fun updateNotification() {
        notificationManager?.notify(NOTIFICATION_ID, createMediaNotification())
    }

    override fun onBind(intent: Intent?): IBinder? = null

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
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
        myMediaSession.release()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    data class PlaybackPayload(
        val videoId: String,
        val mediaUrl: String,
        val title: String,
        val channelName: String,
        val views: String,
        val date: String,
        val duration: String
    )

    companion object {
        private const val TAG = "OnlineBackgroundPlayer"
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

        fun Context.playAudioFromUrl(
            audioUrl: String,
            selectedItem: VideosListData
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

        fun Context.playAudioFromUrl(
            id: String,
            audioUrl: String,
            selectedItem: VideoUiState
        ) {
            startAudioService(
                videoId = id,
                audioUrl = audioUrl,
                title = selectedItem.title,
                channelName = selectedItem.channelName,
                views = selectedItem.views,
                date = selectedItem.date,
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
                action = ACTION_START
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
    }
}
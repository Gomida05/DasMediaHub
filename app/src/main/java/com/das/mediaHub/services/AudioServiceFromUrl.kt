package com.das.mediaHub.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.das.mediaHub.MainActivity
import com.das.mediaHub.R.drawable
import com.das.mediaHub.mediacontroller.MediaSessionPlaybackState
import com.das.mediaHub.data.constants.Action.ACTION_ADD_TO_WATCH_LATER
import com.das.mediaHub.data.constants.Action.ACTION_KILL
import com.das.mediaHub.data.constants.Action.ACTION_START
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.das.mediaHub.receive.Receiver
import com.das.mediaHub.data.constants.Notifications.AUDIO_SERVICE_FROM_URL_NOTIFICATION
import com.das.mediaHub.data.local.DatabaseFavorite
import com.das.python.data.model.VideosListData


@SuppressLint("UnsafeOptInUsageError")
class AudioServiceFromUrl : Service() {

    private val exoPlayer by lazy {
        ExoPlayer.Builder(this)
            .setDeviceVolumeControlEnabled(true)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(AUDIO_CONTENT_TYPE_MOVIE)
                    .build(), true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    private val notificationManager by lazy {
        getSystemService<NotificationManager>()
    }
    private var mediaUrl: String? = null
    private lateinit var videoViews: String
    private lateinit var videoDate: String
    private lateinit var videoId: String
    private lateinit var durationFromActivity: String

    private val myMediaSession by lazy {
        MediaSessionCompat(this, "AudioService").apply {
            isActive = true
            setMediaButtonReceiver(
                PendingIntent.getBroadcast(
                    this@AudioServiceFromUrl, 0,
                    Intent(Intent.ACTION_MEDIA_BUTTON),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

        }
    }

    private val db by lazy {
        DatabaseFavorite(this)
    }

    private val mediaSessionState by lazy {
        MediaSessionPlaybackState(db)
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        mediaUrl = intent?.getStringExtra("media_url")
        val title =  intent?.getStringExtra("title").toString()
        val channelName = intent?.getStringExtra("channelName").toString()

        videoId = intent?.getStringExtra("videoId").toString()
        videoViews = intent?.getStringExtra("viewNumber").toString()
        videoDate = intent?.getStringExtra("videoDate").toString()
        durationFromActivity = intent?.getStringExtra("duration").toString()

        if (mediaUrl.isNullOrBlank()) {
            Log.e("AudioServiceFromUrl", "media_url is null or blank")
            stopSelf()
            return START_NOT_STICKY
        }

        val uri = mediaUrl?.toUri()
        if (uri.toString().isBlank()) {
            Log.e("AudioServiceFromUrl", "Parsed uri is invalid: $mediaUrl")
            stopSelf()
            return START_NOT_STICKY
        }

        val metadata = MediaMetadata.Builder()
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .setDisplayTitle(title)
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(videoId)
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
        val mediaDetails = VideosListData(
            videoId, title,
            videoDate, videoViews,
            durationFromActivity, channelName,
            ""
        )









        exoPlayer.addListener(
            MyExoPlayerCallBack(title, channelName)
        )



        myMediaSession.setCallback(
            MyMediaSessionCallBack(
                mediaDetails
            )
        )



        when (intent?.action) {

            ACTION_START -> {

                exoPlayer.let {
                    it.setMediaItem(mediaItem)
                    it.prepare()
                }
                exoPlayer.play()

                myMediaSession.apply {
                    setPlaybackState(
                        mediaSessionState.setStateToLoading(exoPlayer.currentPosition, videoId)
                    )
                    setMetadata(
                        mediaMetaDetails(
                            title,
                            channelName,
                            exoPlayer.duration
                        )
                    )
                }
            }
            ACTION_KILL ->{
                stopSelf()
            }

        }



        MediaButtonReceiver.handleIntent(myMediaSession, intent)
        val notifications = createMediaNotification()
        startForeground(25, notifications)


        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }



    private fun createMediaNotification(): Notification {


        val deleteIntent = Intent(this, Receiver::class.java).apply {
            action = AUDIO_SERVICE_FROM_URL_NOTIFICATION
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mainIntent = Intent(this, MainActivity::class.java)


        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mediaStyle = MediaStyle()
            .setMediaSession(myMediaSession.sessionToken)





        val notification= NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(drawable.music_note_24dp)
            .setStyle(mediaStyle)
            .setDeleteIntent(deletePendingIntent)
            .setSettingsText("DasMediaHub Media Player")
            .build()


        notificationManager?.notify(NOTIFICATION_ID, notification)
        return notification
    }


    private fun getBitmapFromUrl(url: String, callback: (Bitmap?) -> Unit, gotAnError: (Drawable?) -> Unit) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Pass the loaded bitmap back via the callback
                    callback(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    gotAnError(errorDrawable)
                }
            })
    }



    private fun mediaMetaDetails(
        title: String,
        channelName: String,
        duration: Long
    ): MediaMetadataCompat {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUrl)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, channelName)

            .putString(
                MediaMetadataCompat.METADATA_KEY_ALBUM,
                "unknown album"
            )
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)

        getBitmapFromUrl("https://img.youtube.com/vi/$videoId/0.jpg", { bitmap ->

            if (bitmap != null) {
                metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            } else {
                metadata.putBitmap(
                    MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                    BitmapFactory.decodeResource(resources, drawable.music_note_24dp)
                )
            }
        }, { _ ->
            metadata.putBitmap(
                MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                BitmapFactory.decodeResource(resources, drawable.music_note_24dp)
            )


        }
        )
        return metadata.build()
    }


    inner class MyMediaSessionCallBack(
        private val mediaDetails: VideosListData,
    ): MediaSessionCompat.Callback() {


        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            exoPlayer.seekTo(pos)

        }

        override fun onPlay() {
            super.onPlay()
            exoPlayer.play()
        }

        override fun onPause() {
            super.onPause()
            exoPlayer.pause()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            exoPlayer.seekToPrevious()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            exoPlayer.seekToNext()
        }


        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
            if (action.toString() == ACTION_ADD_TO_WATCH_LATER) {

                myMediaSession.setPlaybackState(
                    mediaSessionState.addItOrRemoveFromDB(
                        exoPlayer.currentPosition,
                        mediaDetails
                    )
                )
                createMediaNotification()
            } else if (action.toString() == ACTION_KILL) {
                stopSelf()
            }
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            if (mediaButtonEvent == null) return super.onMediaButtonEvent(mediaButtonEvent)
            val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            else @Suppress("DEPRECATION")
            mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)

            if (mediaButtonEvent.action == Intent.ACTION_MEDIA_BUTTON && keyEvent != null) {
                // Extract the key event from the intent
                when (keyEvent.keyCode) {

                    KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                        exoPlayer.pause()
                    }

                    KeyEvent.KEYCODE_MEDIA_PLAY -> {
                        exoPlayer.play()
                    }

                    KeyEvent.KEYCODE_MEDIA_NEXT -> {
                        onSkipToNext()
                    }

                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        exoPlayer.seekToPrevious()
                    }
                }
                return true
            }
            return super.onMediaButtonEvent(mediaButtonEvent)

        }
    }

    private inner class MyExoPlayerCallBack(
        private val title: String,
        private val channelName: String
    ): Player.Listener {

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            val currentPosition = newPosition.positionMs

            myMediaSession.setPlaybackState(
                mediaSessionState.setStateToPlaying(currentPosition, videoId)
            )
        }




        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            val currentPosition = exoPlayer.currentPosition

            if (playbackState == Player.STATE_BUFFERING) {
                myMediaSession.apply {
                    setPlaybackState(
                        mediaSessionState.setStateToLoading(currentPosition, videoId)
                    )
                    isActive = true
                }
            } else if (playbackState == Player.STATE_ENDED) {
                myMediaSession.apply {
                    setPlaybackState(
                        mediaSessionState.setStateToPaused(currentPosition, videoId)
                    )
                    isActive = false
                }
            }
            createMediaNotification()
        }


        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            myMediaSession.setPlaybackState(
                mediaSessionState.setStateToLoading(error.timestampMs, videoId)
            )
            createMediaNotification()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            val currentPosition = exoPlayer.currentPosition
            val currentDuration = exoPlayer.duration

            if (isPlaying) {
                myMediaSession.apply {
                    setPlaybackState(
                        mediaSessionState.setStateToPlaying(currentPosition, videoId)
                    )
                    setMetadata(
                        mediaMetaDetails(title, channelName, currentDuration)
                    )
                }
            } else if (exoPlayer.isLoading) {
                myMediaSession.apply {
                    setPlaybackState(
                        mediaSessionState.setStateToLoading(currentPosition, videoId)
                    )
                    setMetadata(
                        mediaMetaDetails(title, channelName, currentDuration)
                    )
                }
            } else {
                myMediaSession.apply {
                    setPlaybackState(
                        mediaSessionState.setStateToPaused(currentPosition, videoId)
                    )
                    setMetadata(
                        mediaMetaDetails(title, channelName, currentDuration)
                    )
                }
            }
            createMediaNotification()
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            super.onIsLoadingChanged(isLoading)
            val currentPosition = exoPlayer.currentPosition
            val currentDuration = exoPlayer.duration

            if (!isLoading) {
                myMediaSession.apply {
                    setPlaybackState(
                        mediaSessionState.setStateToPlaying(currentPosition, videoId)
                    )
                    setMetadata(
                        mediaMetaDetails(title, channelName, currentDuration)
                    )
                }
            } else {
                myMediaSession.setPlaybackState(
                    mediaSessionState.setStateToLoading(currentPosition, videoId)
                )
            }
            createMediaNotification()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        myMediaSession.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (exoPlayer.isPlaying){
            stopSelf()
        }
    }

    private companion object {
        const val CHANNEL_ID = "MediaYouTubePlayer"
        const val NOTIFICATION_ID = 25
    }
}

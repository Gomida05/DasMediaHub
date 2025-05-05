package com.das.forui.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.das.forui.MainActivity
import com.das.forui.R.drawable
import com.das.forui.mediacontroller.MediaSessionPlaybackState
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_ADD_TO_WATCH_LATER
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_KILL
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_START
import com.das.forui.objectsAndData.ForUIDataClass.VideosListData
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.das.forui.objectsAndData.ForUIKeyWords.AUDIO_SERVICE_FROM_URL_NOTIFICATION


class AudioServiceFromUrl : Service() {

    private val channelId = "MediaYouTubePlayer"
    private var exoPlayer: ExoPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioManager: AudioManager
    private lateinit var mediaUrl: String
    private lateinit var videoViews: String
    private lateinit var videoDate: String
    private lateinit var videoId: String
    private lateinit var durationFromActivity: String
    private var audioFocusRequest: AudioFocusRequest? = null
    private lateinit var mediaType: String


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(AudioManager::class.java)
        exoPlayer = ExoPlayer.Builder(this).build()


    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        mediaSession = MediaSessionCompat(this, "AudioService").apply {
            isActive = true
            @Suppress("DEPRECATION")
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            setMediaButtonReceiver(PendingIntent.getBroadcast(
                this@AudioServiceFromUrl, 0,
                Intent(Intent.ACTION_MEDIA_BUTTON),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
        }

        mediaUrl = intent?.getStringExtra("media_url").orEmpty()
        val title =  intent?.getStringExtra("title").toString()
        val channelName = intent?.getStringExtra("channelName").toString()
        val mediaItem = MediaItem.fromUri(mediaUrl)
        videoId = intent?.getStringExtra("videoId").toString()
        videoViews = intent?.getStringExtra("viewNumber").toString()
        videoDate = intent?.getStringExtra("videoDate").toString()
        durationFromActivity = intent?.getStringExtra("duration").toString()


        val mediaDetails = VideosListData(
            videoId, title,
            videoDate, videoViews,
            durationFromActivity, channelName,
            ""
        )









        exoPlayer?.addListener(
            MyExoPlayerCallBack(title, channelName)
        )



        mediaSession.setCallback(
            MyMediaSessionCallBack(
                mediaDetails
            )
        )



        when (intent?.action) {

            ACTION_START -> {

                exoPlayer?.let {
                    it.setMediaItem(mediaItem)
                    it.prepare()
                }
                exoPlayer?.play()

                mediaSession.setPlaybackState(
                    MediaSessionPlaybackState(this).setStateToLoading(exoPlayer?.currentPosition!!, videoId)
                )
                mediaSession.setMetadata(
                    mediaMetaDetails(
                        title,
                        channelName,
                        exoPlayer?.duration!!
                    )
                )
            }
            ACTION_KILL ->{
                mediaSession.release()
                exoPlayer?.release()
                stopSelf()
                stopForeground(STOP_FOREGROUND_REMOVE)
            }

        }



        MediaButtonReceiver.handleIntent(mediaSession, intent)
        val notifications = createMediaNotification()
        startForeground(25, notifications)





        return START_STICKY
    }






    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Media Player",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                group = "MNGC"
                enableLights(false)
                enableVibration(false)
                setSound(null, null)


            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }




    private fun createMediaNotification(): Notification {


        val deleteIntent = Intent(this, BroadReceiverForNotificationActivity::class.java).apply {
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
            .setMediaSession(mediaSession.sessionToken)





        val notification= NotificationCompat.Builder(this, channelId)
            .setContentIntent(pendingIntent)
            .setSmallIcon(drawable.music_note_24dp)
            .setStyle(mediaStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDeleteIntent(deletePendingIntent)
            .setSettingsText("ForUI Media Player")
            .build()


        getSystemService(NotificationManager::class.java).notify(25, notification)
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

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->

        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Audio focus gained, resume playback if it was paused
                if (exoPlayer?.playWhenReady == false) {
                    exoPlayer?.play()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (exoPlayer?.playWhenReady == true) {
                    exoPlayer?.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Temporarily lost audio focus (e.g., incoming call), pause playback
                if (exoPlayer?.playWhenReady == true) {
                    exoPlayer?.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lost audio focus but can duck (e.g., lower volume)
                if (exoPlayer?.playWhenReady == true) {
                    exoPlayer?.volume = 0.1f // Lower volume when focus is lost transiently
                }
            }
        }
    }



    private fun releaseAudioFocusForAudioService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
                audioFocusRequest = null
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun requestAudioFocusForAudioService() {
        audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)

                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()

            val focusRequestResult = audioManager.requestAudioFocus(audioFocusRequest!!)
            if (focusRequestResult == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
//                Log.e("AudioService", "Failed to gain audio focus")
            }
        } else {

            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
//                Log.e("AudioService", "Failed to gain audio focus")
            }
        }
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
            exoPlayer?.seekTo(pos)

        }

        override fun onPlay() {
            super.onPlay()
            mediaSession.isActive = true
            exoPlayer?.play()
        }

        override fun onPause() {
            super.onPause()
            mediaSession.isActive = false
            exoPlayer?.pause()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            exoPlayer?.seekToPrevious()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            exoPlayer?.seekToNext()
        }



        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
            if (action.toString() == ACTION_ADD_TO_WATCH_LATER) {

                mediaSession.setPlaybackState(
                    MediaSessionPlaybackState(this@AudioServiceFromUrl).addItOrRemoveFromDB(
                        exoPlayer?.currentPosition!!,
                        mediaDetails
                    )
                )
                createMediaNotification()
            }
            else if (action.toString() == ACTION_KILL){
                exoPlayer?.let {
                    it.stop()
                    it.release()
                }
                stopSelf()
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.cancel(1)
                onDestroy()
            }
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            mediaButtonEvent?.let {
                @Suppress("DEPRECATION")
                val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    it.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                else it.getParcelableExtra(Intent.EXTRA_KEY_EVENT)

                if (it.action == Intent.ACTION_MEDIA_BUTTON) {
                    // Extract the key event from the intent
                    keyEvent?.let { event ->
                        when (event.keyCode) {

                            KeyEvent.KEYCODE_MEDIA_PAUSE ->{
                                exoPlayer?.pause()
                            }

                            KeyEvent.KEYCODE_MEDIA_PLAY ->{
                                exoPlayer?.play()
                            }

                            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                                onSkipToNext()
                                return true
                            }

                            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                                exoPlayer?.seekToPrevious()
                                return true
                            }

                            else -> {
                                return true
                            }
                        }
                    }
                }
            }
            // If the event is not handled, call the superclass method
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

            mediaSession.setPlaybackState(
                MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPlaying(
                    currentPosition, videoId
                )
            )
        }




        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            if (playbackState == Player.STATE_BUFFERING) {
                mediaSession.setPlaybackState(
                    MediaSessionPlaybackState(this@AudioServiceFromUrl)
                        .setStateToLoading(
                            exoPlayer?.currentPosition!!, videoId
                        )
                )
                mediaSession.isActive = true
            } else if (playbackState == Player.STATE_ENDED) {
                mediaSession.setPlaybackState(
                    MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPaused(
                        exoPlayer?.currentPosition!!, videoId
                    )
                )
                mediaSession.isActive = false
            }
            createMediaNotification()
        }


        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            mediaSession.setPlaybackState(
                MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToLoading(
                    error.timestampMs, videoId
                )
            )
            createMediaNotification()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)

            if (isPlaying) {
                requestAudioFocusForAudioService()
                mediaSession.setPlaybackState(
                    MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPlaying(
                        exoPlayer?.currentPosition!!, videoId
                    )
                )
                mediaSession.setMetadata(
                    mediaMetaDetails(
                        title,
                        channelName,
                        exoPlayer?.duration!!
                    )
                )
            }

            if (!isPlaying && exoPlayer?.isLoading!!) {
                releaseAudioFocusForAudioService()
                mediaSession.setPlaybackState(
                    MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToLoading(
                        exoPlayer?.currentPosition!!, videoId
                    )
                )
                mediaSession.setMetadata(
                    mediaMetaDetails(
                        title,
                        channelName,
                        exoPlayer?.duration!!
                    )
                )
            }

            if (!isPlaying) {
                releaseAudioFocusForAudioService()
                mediaSession.setPlaybackState(
                    MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPaused(
                        exoPlayer?.currentPosition!!, videoId
                    )
                )
                mediaSession.setMetadata(
                    mediaMetaDetails(
                        title,
                        channelName,
                        exoPlayer?.duration!!
                    )
                )
            }
            createMediaNotification()
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            super.onIsLoadingChanged(isLoading)
            if (!isLoading) {
                requestAudioFocusForAudioService()
                mediaSession.setPlaybackState(
                    MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPlaying(
                        exoPlayer?.currentPosition!!, videoId
                    )
                )
                mediaSession.setMetadata(
                    mediaMetaDetails(
                        title, channelName,
                        exoPlayer?.duration!!
                    )
                )
            } else {
                mediaSession.setPlaybackState(
                    MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToLoading(
                        exoPlayer?.currentPosition!!, videoId
                    )
                )
            }
            createMediaNotification()
        }
    }



    override fun stopService(name: Intent?): Boolean {
        mediaSession.release()
        exoPlayer?.release()
        return super.stopService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        exoPlayer?.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!exoPlayer?.isPlaying!!){
            exoPlayer?.release()
            mediaSession.release()
            stopSelf()
        }
    }
}

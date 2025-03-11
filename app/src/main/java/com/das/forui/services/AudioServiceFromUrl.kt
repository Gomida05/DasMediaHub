@file:Suppress("DEPRECATION")
package com.das.forui.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.das.forui.MainActivity
import com.das.forui.R.drawable
import com.das.forui.mediacontroller.MediaSessionPlaybackState
import com.das.forui.ui.viewer.ViewerFragment.Video
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlin.properties.Delegates

class AudioServiceFromUrl : Service() {

    private val channelId = "MediaYouTubePlayer"
    var exoPlayer: ExoPlayer? = null
    lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioManager: AudioManager
    private lateinit var mediaUrl: String
    private lateinit var videoViews: String
    private lateinit var videoDate: String
    lateinit var videoId: String
    private lateinit var durationFromActivity: String
    private var audioFocusRequest: AudioFocusRequest? = null
    private var exoPlayerDuration by Delegates.notNull<Long>()


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(AudioManager::class.java)
        exoPlayer = ExoPlayer.Builder(this).build()

        mediaSession = MediaSessionCompat(this, "AudioService").apply {
            isActive = true
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            setMediaButtonReceiver(PendingIntent.getBroadcast(
                this@AudioServiceFromUrl, 0,
                Intent(Intent.ACTION_MEDIA_BUTTON),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
        }








    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        mediaUrl = intent?.getStringExtra("media_url").orEmpty()
        val title=  intent?.getStringExtra("title").toString()
        val channelName= intent?.getStringExtra("channelName").toString()
        val mediaItem = MediaItem.fromUri(mediaUrl)
        videoId = intent?.getStringExtra("videoId").toString()
        videoViews = intent?.getStringExtra("viewNumber").toString()
        videoDate = intent?.getStringExtra("videoDate").toString()
        durationFromActivity = intent?.getStringExtra("duration").toString()
        exoPlayerDuration = intent?.getLongExtra("exoPlayerDuration", exoPlayer?.duration!!)!!












        exoPlayer?.addListener(object : Player.Listener {


            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                val currentPosition = newPosition.contentPositionMs


                mediaSession.setPlaybackState(
                    MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPlaying(
                        currentPosition, videoId
                    )
                )
            }


            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                if (playbackState == Player.STATE_ENDED) {
                    mediaSession.setPlaybackState(
                        MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPaused(
                            exoPlayer?.currentPosition!!, videoId
                        )
                    )
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (playWhenReady) {

                    requestAudioFocusForAudioService()
                    mediaSession.setPlaybackState(
                        MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPlaying(
                            exoPlayer?.currentPosition!!, videoId
                        )
                    )
                } else {
                    releaseAudioFocusForAudioService()
                    mediaSession.setPlaybackState(
                        MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPaused(
                            exoPlayer?.currentPosition!!, videoId
                        )
                    )
                }


                val metadatas = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUrl)
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_TITLE,
                        title
                    ) // Song/Video title
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_ARTIST,
                        channelName
                    )
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM,
                        "unknown album"
                    )
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayerDuration)


                getBitmapFromUrl("https://img.youtube.com/vi/$videoId/0.jpg") { bitmap ->

                    if (bitmap != null) {
                        metadatas.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                    } else {

                        metadatas.putBitmap(
                            MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                            BitmapFactory.decodeResource(resources, drawable.music_note_24dp)
                        )
                    }
                    mediaSession.setMetadata(metadatas.build())
                }
            }
        }
        )


        val mediaDetails = Video(
            videoId, title, videoDate,
            videoViews, durationFromActivity, channelName,
            ""
        )
        val mediaSessionCallBack = MyMediaSessionCallBack(
            this,
            exoPlayer,
            mediaSession,
            mediaDetails,
            this
        )
        mediaSession.setCallback(mediaSessionCallBack)


        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUrl)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title) // Song/Video title
            .putString(
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                channelName
            )
            .putString(
                MediaMetadataCompat.METADATA_KEY_ALBUM,
                "unknown album"
            )
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayerDuration)


        getBitmapFromUrl("https://img.youtube.com/vi/$videoId/0.jpg") { bitmap ->

            if (bitmap != null) {
                metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            } else {

                metadata.putBitmap(
                    MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                    BitmapFactory.decodeResource(resources, drawable.music_note_24dp)
                )
            }
            mediaSession.setMetadata(metadata.build())
        }


        when (intent.action) {

            ACTION_START -> {

                exoPlayer?.let {
                    it.setMediaItem(mediaItem)
                    it.prepare()
                    it.play()
                }

                mediaSession.setPlaybackState(
                    MediaSessionPlaybackState(this).setStateToPlaying(exoPlayer?.currentPosition!!, videoId)
                )
                val metal = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUrl)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title) // Song/Video title
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_ARTIST,
                        channelName
                    )
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM,
                        "unknown album"
                    )
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayerDuration)


                getBitmapFromUrl("https://img.youtube.com/vi/$videoId/0.jpg") { bitmap ->

                    if (bitmap != null) {
                        metal.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                    } else {

                        metal.putBitmap(
                            MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                            BitmapFactory.decodeResource(resources, drawable.music_note_24dp)
                        )
                    }
                    mediaSession.setMetadata(metal.build())
                }
            }

        }




        val notifications = createMediaNotification()
        startForeground(1, notifications)
        return START_STICKY
    }






    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }




    private fun createMediaNotification(): Notification {



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
            .build()


        getSystemService(NotificationManager::class.java).notify(1, notification)
        return notification
    }


    private fun getBitmapFromUrl(url: String, callback: (Bitmap?) -> Unit) {
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
                Log.e("AudioService", "Failed to gain audio focus")
            }
        } else {
            // For older versions, use the deprecated method
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                Log.e("AudioService", "Failed to gain audio focus")
            }
        }
    }


    inner class MyMediaSessionCallBack(
        private val context: Context,
        private val exoPlayer: ExoPlayer?,
        private val mediaSession: MediaSessionCompat,
        private val mediaDetails: Video,
        private val service: Service
    ): MediaSessionCompat.Callback() {
        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            exoPlayer?.seekTo(pos)
        }

        override fun onPlay() {
            super.onPlay()

            requestAudioFocusForAudioService()
            exoPlayer?.play()
            mediaSession.setPlaybackState(
                MediaSessionPlaybackState(context).setStateToPlaying(exoPlayer?.currentPosition!!, mediaDetails.videoId)
            )
        }

        override fun onPause() {
            super.onPause()
            println("here is it from1")
            releaseAudioFocusForAudioService()
            exoPlayer?.pause()
            mediaSession.setPlaybackState(
                MediaSessionPlaybackState(context).setStateToPaused(exoPlayer?.currentPosition!!, mediaDetails.videoId)
            )
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
                    MediaSessionPlaybackState(context).addItOrRemoveFromDB(
                        exoPlayer?.currentPosition!!,
                        mediaDetails
                    )
                )

            }
            else if (action.toString() == ACTION_KILL){
                exoPlayer?.let {
                    it.stop()
                    it.release()
                }
                service.stopSelf()
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.cancel(1)

            }
        }


    }


    companion object {

        const val ACTION_START= "com.das.forui.Start"
        const val ACTION_PREVIOUS = "com.das.forui.Previous"
        const val ACTION_PLAY = "com.das.forui.PLAY"
        const val ACTION_PAUSE = "com.das.forui.PAUSE"
        const val ACTION_NEXT = "com.das.forui.STOP"
        const val ACTION_KILL= "com.das.forui.kill"
        const val ACTION_ADD_TO_WATCH_LATER ="com.das.forui.ACTION_ADD_TO_WATCH_LATER"
    }



    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }
}

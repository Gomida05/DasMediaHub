@file:Suppress("DEPRECATION")
package com.das.forui.services

import android.app.Notification
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
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

class AudioServiceFromUrl : Service() {

    private val channelId = "MediaYouTubePlayer"
    private var exoPlayer: ExoPlayer? = null
   private lateinit var mediaSession: MediaSessionCompat
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var audioManager: AudioManager
    private lateinit var mediaUrl: String
    private lateinit var videoViews: String
    private lateinit var videoDate: String
    private lateinit var videoId: String
    private lateinit var durationFromActivity: String
    private var audioFocusRequest: AudioFocusRequest? = null


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(AudioManager::class.java)
        exoPlayer = ExoPlayer.Builder(this).build()
        mediaSession = MediaSessionCompat(this, "AudioService").apply {
            isActive = true
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
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayer?.duration!!)


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

                if (playbackState == Player.STATE_ENDED){
                    mediaSession.setPlaybackState(
                        MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPaused(
                            exoPlayer?.currentPosition!!, videoId
                        )
                    )
                }
            }
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (playWhenReady){
                    val metadatas = MediaMetadataCompat.Builder()
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
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayer?.duration!!)


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
                    requestAudioFocus()
                    mediaSession.setPlaybackState(
                        MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPlaying(
                            exoPlayer?.currentPosition!!, videoId
                        )
                    )
                }else{
                    releaseAudioFocus()
                    mediaSession.setPlaybackState(
                        MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPaused(
                            exoPlayer?.currentPosition!!, videoId
                        )
                    )
                    val metadatas = MediaMetadataCompat.Builder()
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
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayer?.duration!!)


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
        }

        )





        mediaSession.setCallback(object : MediaSessionCompat.Callback() {



            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                exoPlayer?.seekTo(pos)
            }

            override fun onPlay() {
                super.onPlay()
                requestAudioFocus()
                exoPlayer?.play()

            }




            override fun onCustomAction(action: String?, extras: Bundle?) {
                super.onCustomAction(action, extras)
                val currentPosition = exoPlayer?.currentPosition!!
                if (action.toString() == ACTION_PLAY){
                    exoPlayer?.play()
                    mediaSession.setPlaybackState(
                        MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPlaying(currentPosition, videoId)
                    )


                }
                else if (action.toString() == ACTION_PAUSE){
                    exoPlayer?.pause()
                    mediaSession.setPlaybackState(
                        MediaSessionPlaybackState(this@AudioServiceFromUrl).setStateToPaused(currentPosition, videoId)
                    )
                }
                else if (action.toString() == ACTION_PREVIOUS){
                    exoPlayer?.seekToPrevious()
                }
                else if (action.toString() == ACTION_NEXT){
                    exoPlayer?.seekToNext()
                }
                else if (action.toString() == ACTION_ADD_TO_WATCH_LATER) {

                    mediaSession.setPlaybackState(
                        MediaSessionPlaybackState(this@AudioServiceFromUrl).addItOrRemoveFromDB(
                            exoPlayer?.currentPosition!!,
                            Video(
                                videoId, title, videoDate,
                                videoViews, durationFromActivity, channelName,
                                ""
                            )
                        )
                    )

                }
                else if (action.toString() == ACTION_KILL){
                    stopSelf()
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager?.cancel(1)

                }
            }



            override fun onPause() {
                super.onPause()
                println("here is it from1")
                releaseAudioFocus()
                handler.removeCallbacksAndMessages(this)
                exoPlayer?.pause()
            }


            override fun onStop() {
                super.onStop()
                releaseAudioFocus()
                println("it is pausing1")
                exoPlayer?.let {
                    it.stop()
                    it.release()
                }
                stopSelf()

            }
        })

        when (intent?.action) {

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
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayer?.duration!!)


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
            .setShowActionsInCompactView(1,2,3)




        val notification= NotificationCompat.Builder(this, channelId)
            .setContentIntent(pendingIntent)
            .setSmallIcon(drawable.music_note_24dp)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(mediaStyle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
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


    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
                audioFocusRequest = null
            }
        } else {
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    private fun requestAudioFocus() {
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

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

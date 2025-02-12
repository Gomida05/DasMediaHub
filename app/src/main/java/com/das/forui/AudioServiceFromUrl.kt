@file:Suppress("DEPRECATION")
package com.das.forui

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
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.graphics.convertTo
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlin.properties.Delegates

class AudioServiceFromUrl : Service() {

    private val channelId = "MediaYouTubePlayer"
    private var exoPlayer: ExoPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var audioManager: AudioManager
    private var isAdded by Delegates.notNull<Boolean>()
    private lateinit var mediaUrl: String
    private lateinit var videoViews: String
    private lateinit var videoDate: String
    private lateinit var videoId: String
    private lateinit var duration: String
    private var audioFocusRequest: AudioFocusRequest? = null
    private var actionIcon by Delegates.notNull<Int>()


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(AudioManager::class.java)
        mediaSession = MediaSessionCompat(this, "AudioService")
        mediaSession.isActive = true
        exoPlayer = ExoPlayer.Builder(this).build()



    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {



        val action = intent?.action
        mediaUrl = intent?.getStringExtra("media_url").orEmpty()
        val audiUrl= mediaUrl
        val title=  intent?.getStringExtra("title").toString()
        val channelName= intent?.getStringExtra("channelName").toString()
        val mediaItem = MediaItem.fromUri(mediaUrl)
        videoId = intent?.getStringExtra("videoId").toString()
        videoViews = intent?.getStringExtra("viewNumber").toString()
        videoDate = intent?.getStringExtra("videoDate").toString()
        duration = intent?.getStringExtra("duration").toString()
        isAdded= isAdded(videoId)
        actionIcon = if (isAdded(videoId)){
            R.drawable.favorite
        } else{
            R.drawable.un_favorite_icon
        }
//        actionIcon = if (isAdded(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon
        println("it's is here $isAdded")



        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                requestAudioFocus()
                exoPlayer?.play()
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
                exoPlayer?.stop()
                exoPlayer?.release()
                stopSelf()
            }
        })

        val notifications=createMediaNotification(title, channelName, audiUrl)

        startForeground(1, notifications)
        when (action) {

            ACTION_START -> {
                if (exoPlayer == null) {
                    // Reinitialize ExoPlayer if it's null
                    exoPlayer = ExoPlayer.Builder(this).build()
                    exoPlayer?.setMediaItem(mediaItem)
                    exoPlayer?.prepare()
                    exoPlayer?.play()
                    actionIcon = if (isAdded(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon
                } else {
                    actionIcon = if (isAdded(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon
                    exoPlayer?.setMediaItem(mediaItem)
                    exoPlayer?.prepare()
                    exoPlayer?.play()
                    exoPlayer?.addListener(object : Player.Listener {
                        override fun onPositionDiscontinuity(reason: Int) {
                            super.onPositionDiscontinuity(reason)
                            val currentPosition = exoPlayer?.currentPosition ?: 0L
                            val duration = exoPlayer?.duration ?: 0L
                            println("duration $duration")
                            println("current Position $currentPosition")
                            // Update the notification with the current progress
//                getSystemService(NotificationManager::class.java).notify(1, createMediaNotification(title, channelName, audiUrl, hasBeenAdded, currentPosition, duration))
                        }


                        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                            super.onPlayWhenReadyChanged(playWhenReady, reason)
                            if (playWhenReady ==true){
                                requestAudioFocus()
                                println("service playing")
                            }else{
                                releaseAudioFocus()
                                println("service pausing")
                            }
                        }
                    })
                }
            }
            ACTION_PREVIOUS -> {
                exoPlayer?.previous()
            }

            ACTION_PAUSE_PLAY -> {
                println("media Item $mediaUrl")

                if (exoPlayer?.isPlaying == false) {
                    println(" it is still not playing")
                    exoPlayer?.playWhenReady=true
                }else{
                    exoPlayer?.pause()

                    println("it is still pausing")
                }
            }

            ACTION_NEXT -> {
                exoPlayer?.next()
            }

            ACTION_KILL ->{
                stopSelf()
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.cancel(1)
            }
            ACTION_ADD_TO_WATCH_LATER->{
                val dBase= DatabaseFavorite(this)
                if (isAdded(videoId)) {
                    dBase.deleteWatchUrl(videoId)
                    actionIcon= R.drawable.un_favorite_icon
                    isAdded=false
                    println("it's here ${isAdded(videoId)} and $isAdded")
                } else {
                    dBase.insertData(videoId, title, videoDate, videoViews, channelName, duration)
                    isAdded=true
                    actionIcon= R.drawable.favorite
                    println("it's is here and $isAdded")
                }
            }

            ACTION_DELETE_INTENT->{
//                println("Notification deleted by the user.")

                if (exoPlayer?.isPlaying == false){
                    stopSelf()
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager?.cancel(1)
                }

            }
        }




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
                NotificationManager.IMPORTANCE_HIGH).apply {
                setShowBadge(false)
                description = "channelDescription"
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
    private fun isAdded(videoId: String): Boolean{
        println("the provided url is ${DatabaseFavorite(this).isWatchUrlExist(videoId)}")
        return DatabaseFavorite(this).isWatchUrlExist(videoId)

    }



    private fun createMediaNotification(
        title: String,
        channelName: String,
        audiUrl: String
    ): Notification {


        val previousIntent = Intent(this, AudioServiceFromUrl::class.java).apply {
            action = ACTION_PREVIOUS
        }

        val previousPendingIntent = PendingIntent.getService(this, 0, previousIntent,PendingIntent.FLAG_MUTABLE)

        val pauseIntent = Intent(this, AudioServiceFromUrl::class.java).apply {
            action = ACTION_PAUSE_PLAY
            putExtra("title", title)
            putExtra("media_url", audiUrl)
            putExtra("videoId", videoId)
            putExtra("channelName", channelName)
            putExtra("viewNumber", videoViews)
            putExtra("videoDate", videoDate)
            putExtra("duration", duration)
        }
        val pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val nextIntent = Intent(this, AudioServiceFromUrl::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(
            this,
            0,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = Intent(this, AudioServiceFromUrl::class.java).apply {
            action = ACTION_KILL
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_MUTABLE
        )

        val actionText = if (isAdded) "addIt" else "removeIt"

        val watchLaterIntent = Intent(this, AudioServiceFromUrl::class.java).apply {
            action = ACTION_ADD_TO_WATCH_LATER
            putExtra("videoId", videoId)
            putExtra("is_added", isAdded(videoId))
            putExtra("title", title)
            putExtra("media_url", audiUrl)
            putExtra("channelName", channelName)
            putExtra("viewNumber", videoViews)
            putExtra("videoDate", videoDate)
            putExtra("duration", duration)
        }


        val actionIntent = PendingIntent.getService(
            this,
            0,
            watchLaterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val deleteIntent = Intent(this, AudioServiceFromUrl::class.java).apply {
            action = ACTION_DELETE_INTENT
            putExtra("title", title)
            putExtra("media_url", audiUrl)
            putExtra("videoId", videoId)
            putExtra("channelName", channelName)
            putExtra("viewNumber", videoViews)
            putExtra("videoDate", videoDate)
            putExtra("duration", duration)
        }

        val deletePendingIntent = PendingIntent.getService(
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

        val fullScreenIntent = Intent(this, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
            fullScreenIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val duration = exoPlayer?.duration!!.toLong()


        val metadata = MediaMetadataCompat.Builder()
            .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, channelName)
            .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, title)
            .putBitmap("gg", BitmapFactory.decodeResource(this.resources, R.drawable.favorite))
            .build()
        mediaSession.setMetadata(metadata)
        val mediaStyle = MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(1, 2, 3)
//        MediaMetadataCompat.fromMediaMetadata(metadata)
        val notification= NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(channelName)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.music_note_24dp)
            .setOngoing(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setStyle(mediaStyle)
            .addAction(NotificationCompat.Action(
                if (isAdded(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon,
                actionText, actionIntent
            ))
            .addAction(R.drawable.skip_previous_24dp, "Previous", previousPendingIntent)
            .addAction(NotificationCompat.Action(
                if (exoPlayer?.isPlaying == true) R.drawable.play_arrow_24dp else R.drawable.pause_icon,
                "Play/Pause", pausePendingIntent
            ))
            .addAction(R.drawable.skip_next_24dp, "Next", nextPendingIntent)
            .addAction(R.drawable.stop_circle_24dp, "Stop", stopPendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setAutoCancel(false)
            .setSound(null)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDeleteIntent(deletePendingIntent)
            .setVibrate(longArrayOf(0))
            .setProgress(100, 75, false)
            .build()




        getSystemService(NotificationManager::class.java).notify(1, notification)
        return notification
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

//    private fun releaseAudioFocus() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
//        } else {
//            audioManager.abandonAudioFocus(audioFocusChangeListener)
//        }
//    }
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
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
        const val ACTION_PREVIOUS = "com.das.forui.PLAY"
        const val ACTION_PAUSE_PLAY = "com.das.forui.PAUSE"
        const val ACTION_NEXT = "com.das.forui.STOP"
        const val ACTION_KILL= "com.das.forui.kill"
        const val ACTION_ADD_TO_WATCH_LATER ="com.das.forui.ACTION_ADD_TO_WATCH_LATER"
        const val ACTION_DELETE_INTENT = "com.das.forui.DELETE_INTENT"
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }
}

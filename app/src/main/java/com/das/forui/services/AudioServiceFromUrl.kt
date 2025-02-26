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
import android.content.Intent.EXTRA_TEXT
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.databased.DatabaseFavorite
import com.das.forui.ui.viewer.ViewerFragment
import kotlin.properties.Delegates

class AudioServiceFromUrl : Service() {

    private val channelId = "MediaYouTubePlayer"
    var exoPlayerFromAudioService: ExoPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var audioManager: AudioManager
    private var isAdded by Delegates.notNull<Boolean>()
    private lateinit var mediaUrl: String
    private lateinit var videoViews: String
    private lateinit var videoDate: String
    private lateinit var videoId: String
    private lateinit var durationFromActivity: String
    private var audioFocusRequest: AudioFocusRequest? = null
    var isForeGroundAudioService = false


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(AudioManager::class.java)
        mediaSession = MediaSessionCompat(this, "AudioService")
        mediaSession.isActive = true
        exoPlayerFromAudioService = ExoPlayer.Builder(this).build()



    }


    fun formatTimeToFloat(milliseconds: Long): Float {
        return milliseconds / 1000f
    }

    @UnstableApi
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
        durationFromActivity = intent?.getStringExtra("duration").toString()
        isAdded= isAdded(videoId)


        val duration = exoPlayerFromAudioService?.duration!!.toLong()



        val metadata = MediaMetadataCompat
            .Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUrl)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title) // Song/Video title
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, channelName) // Channel name as the artist
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "unknown album") // Optional, if available
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration) // Song/Video duration
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, audiUrl)


        getBitmapFromUrl("https://img.youtube.com/vi/$videoId/0.jpg") { bitmap ->
            // Once the bitmap is loaded, we can set it on the metadata
            if (bitmap != null) {
                metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            } else {
                // Handle case where the bitmap is null (e.g., show a default image)
                metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(resources, R.drawable.music_note_24dp))
            }

            mediaSession.setMetadata(metadata.build())
            // Update the media session with the metadata after the bitmap is set
        }







        exoPlayerFromAudioService?.addListener(object : Player.Listener {


            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                val currentPosition = newPosition.contentPositionMs


                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, currentPosition,
                            1F
                        )
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .addCustomAction(ACTION_PAUSE, "myPauseButton", R.drawable.pause_icon)
                        .addCustomAction(ACTION_NEXT, "myNextButton", R.drawable.skip_next_24dp)
                        .addCustomAction(ACTION_PREVIOUS, "myPreviousButton", R.drawable.skip_previous_24dp)
                        .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton",
                            if (isAdded(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon
                        )
                        .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
                        .setBufferedPosition(currentPosition)
                        .build()
                )
            }


            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                if (playbackState == Player.STATE_ENDED){
                    mediaSession.setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PAUSED, exoPlayerFromAudioService?.currentPosition!!,
                                1F
                            )
                            .setActions(PlaybackStateCompat.ACTION_STOP)
                            .addCustomAction(ACTION_PLAY, "myPlayButton", R.drawable.play_arrow_24dp)
                            .addCustomAction(ACTION_NEXT, "myNextButton", R.drawable.skip_next_24dp)
                            .addCustomAction(ACTION_PREVIOUS, "myPreviousButton", R.drawable.skip_previous_24dp)
                            .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton",
                                if (isAdded(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon
                            )
                            .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
                            .setBufferedPosition(exoPlayerFromAudioService?.currentPosition!!)
                            .build()
                    )
                }
            }
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (playWhenReady){
                    requestAudioFocus()
                    println("service playing")
                }else{
                    releaseAudioFocus()
                    println("service pausing")
                }
            }
        }

        )




        mediaSession.setCallback(object : MediaSessionCompat.Callback() {



            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                exoPlayerFromAudioService?.seekTo(pos)
            }

            override fun onPlay() {
                super.onPlay()
                requestAudioFocus()
                exoPlayerFromAudioService?.play()

            }

            override fun onCustomAction(action: String?, extras: Bundle?) {
                super.onCustomAction(action, extras)
                if (action.toString() == ACTION_PLAY){
                    exoPlayerFromAudioService?.play()
                    mediaSession.setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PLAYING, exoPlayerFromAudioService?.currentPosition!!,
                                1F
                            )
                            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
//                            .addCustomAction("Fav", "myFavButton", R.drawable.favorite)
                            .addCustomAction(ACTION_PAUSE, "myPauseButton", R.drawable.pause_icon)
                            .addCustomAction(ACTION_NEXT, "myNextButton", R.drawable.skip_next_24dp)
                            .addCustomAction(ACTION_PREVIOUS, "myPreviousButton", R.drawable.skip_previous_24dp)
                            .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton",
                                if (isAdded(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon
                            )
                            .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
                            .setBufferedPosition(exoPlayerFromAudioService?.currentPosition!!)
                            .build()
                    )

                }
                else if (action.toString() == ACTION_PAUSE){
                    exoPlayerFromAudioService?.pause()
                    mediaSession.setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PAUSED, exoPlayerFromAudioService?.currentPosition!!,
                                1F
                            )
                            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                            .addCustomAction(ACTION_PLAY, "myPlayButton", R.drawable.play_arrow_24dp)
                            .addCustomAction(ACTION_NEXT, "myNextButton", R.drawable.skip_next_24dp)
                            .addCustomAction(ACTION_PREVIOUS, "myPreviousButton", R.drawable.skip_previous_24dp)
                            .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton",
                                if (isAdded(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon
                            )
                            .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)

                            .setBufferedPosition(exoPlayerFromAudioService?.currentPosition!!)
                            .build()
                    )
                }
                else if (action.toString() == ACTION_PREVIOUS){
                    exoPlayerFromAudioService?.seekToPrevious()
                }
                else if (action.toString() == ACTION_NEXT){
                    exoPlayerFromAudioService?.seekToNext()
                }
                else if (action.toString() == ACTION_ADD_TO_WATCH_LATER){
                    val db = DatabaseFavorite(this@AudioServiceFromUrl)
                    val playbackSate = PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PLAYING, exoPlayerFromAudioService?.currentPosition!!,
                                1F
                            )
                            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                            .addCustomAction(ACTION_PLAY, "myPlayButton", R.drawable.play_arrow_24dp)
                            .addCustomAction(ACTION_NEXT, "myNextButton", R.drawable.skip_next_24dp)
                            .addCustomAction(ACTION_PREVIOUS, "myPreviousButton", R.drawable.skip_previous_24dp)
                            .setBufferedPosition(exoPlayerFromAudioService?.currentPosition!!)

                    if (isAdded(videoId)){
                        db.deleteWatchUrl(videoId)
                            playbackSate
                                .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton", R.drawable.un_favorite_icon)
                                .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)

                    }
                    else{
                        db.insertData(
                            videoId, title, videoDate,
                            videoViews, channelName, durationFromActivity
                        )
                        playbackSate
                            .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton", R.drawable.favorite)
                            .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)

                    }

                    mediaSession.setPlaybackState(playbackSate.build())

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
                exoPlayerFromAudioService?.pause()
            }

            override fun onStop() {
                super.onStop()
                releaseAudioFocus()
                println("it is pausing1")
                exoPlayerFromAudioService?.let {
                    it.stop()
                    it.release()
                }
                stopSelf()
            }
        })




        when (action) {

            ACTION_START -> {
                if (exoPlayerFromAudioService == null) {
                    // Reinitialize ExoPlayer if it's null
                    exoPlayerFromAudioService = ExoPlayer.Builder(this).build()
                    exoPlayerFromAudioService?.let {
                        it.setMediaItem(mediaItem)
                        it.prepare()
                        it.play()
                    }


                } else {
                    exoPlayerFromAudioService?.let {
                        it.setMediaItem(mediaItem)
                        it.prepare()
                        it.play()
                    }

                }

                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, exoPlayerFromAudioService?.currentPosition!!,
                            1F
                        )
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .addCustomAction(ACTION_PAUSE, "myPauseButton", R.drawable.pause_icon)
                        .addCustomAction(ACTION_NEXT, "myNextButton", R.drawable.skip_next_24dp)
                        .addCustomAction(ACTION_PREVIOUS, "myPreviousButton", R.drawable.skip_previous_24dp)
                        .addCustomAction(ACTION_ADD_TO_WATCH_LATER, "myFavButton",
                            if (isAdded(videoId)) R.drawable.favorite else R.drawable.un_favorite_icon
                        )
                        .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
                        .setBufferedPosition(exoPlayerFromAudioService?.currentPosition!!)
                        .build()
                )
            }





            ACTION_DELETE_INTENT ->{
//                println("Notification deleted by the user.")

                if (exoPlayerFromAudioService?.isPlaying == false){
                    stopSelf()
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager?.cancel(1)
                }

            }
        }



        val notifications = createMediaNotification(title, channelName, audiUrl)

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
                NotificationManager.IMPORTANCE_HIGH).apply {
                setShowBadge(true)
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






        val deleteIntent = Intent(this, AudioServiceFromUrl::class.java).apply {
            action = ACTION_DELETE_INTENT
            putExtra("title", title)
            putExtra("media_url", audiUrl)
            putExtra("videoId", videoId)
            putExtra("channelName", channelName)
            putExtra("viewNumber", videoViews)
            putExtra("videoDate", videoDate)
            putExtra("duration", durationFromActivity)
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



        val mediaStyle = MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(1,2,3)





        val notification= NotificationCompat.Builder(this, channelId)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.music_note_24dp)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setStyle(mediaStyle)
            .setAutoCancel(false)
            .setSound(null)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDeleteIntent(deletePendingIntent)
            .setVibrate(longArrayOf(0))
            .setProgress(100, 75, false)
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
                    // Optionally handle cleanup or pass a default bitmap back
                    val fallbackBitmap = placeholder?.let { drawableToBitmap(it) }
                    callback(fallbackBitmap)
                }
            })
    }

    // Utility function to convert Drawable to Bitmap
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight

        if (width <= 0 || height <= 0) {
            return Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Return a blank Bitmap if drawable has no intrinsic size
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->

        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Audio focus gained, resume playback if it was paused
                if (exoPlayerFromAudioService?.playWhenReady == false) {
                    exoPlayerFromAudioService?.play()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (exoPlayerFromAudioService?.playWhenReady == true) {
                    exoPlayerFromAudioService?.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Temporarily lost audio focus (e.g., incoming call), pause playback
                if (exoPlayerFromAudioService?.playWhenReady == true) {
                    exoPlayerFromAudioService?.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lost audio focus but can duck (e.g., lower volume)
                if (exoPlayerFromAudioService?.playWhenReady == true) {
                    exoPlayerFromAudioService?.volume = 0.1f // Lower volume when focus is lost transiently
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
        const val ACTION_DELETE_INTENT = "com.das.forui.DELETE_INTENT"
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayerFromAudioService?.release()
    }
}

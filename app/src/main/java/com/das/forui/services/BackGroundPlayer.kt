@file:Suppress("DEPRECATION")
package com.das.forui.services

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.das.forui.MainActivity
import com.das.forui.R.drawable
import com.das.forui.databased.PathSaver
import com.das.forui.mediacontroller.BackgroundPlayerStates
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_KILL
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_START
import com.das.forui.objectsAndData.ForUIKeyWords.SET_SHUFFLE_MODE
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import java.io.File


class BackGroundPlayer: Service() {

    private val channelId = "MediaYouTubePlayer"
    private var exoPlayer: ExoPlayer? = null
    lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioManager: AudioManager
    private lateinit var mediaId: String
    private var audioFocusRequest: AudioFocusRequest? = null


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(AudioManager::class.java)
        exoPlayer = ExoPlayer.Builder(this).build()

        mediaSession = MediaSessionCompat(this, "BackGroundPlayer").apply {
            isActive = true
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            setMediaButtonReceiver(PendingIntent.getBroadcast(
                this@BackGroundPlayer, 0,
                Intent(Intent.ACTION_MEDIA_BUTTON),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
        }








    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val mediaUri = intent?.getStringExtra("media_url").orEmpty()
        val title =  intent?.getStringExtra("title").toString()
        mediaId = intent?.getStringExtra("media_id").toString()

        val exoMetadata = MediaMetadata.Builder()
            .setTitle(title)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .build()
        val mediaItem = MediaItem.Builder()
            .setMediaId(mediaId)
            .setUri(mediaUri.toUri())
            .setMediaMetadata(exoMetadata)
            .build()

        exoPlayer?.setMediaItem(mediaItem)













        exoPlayer?.addListener(object : Player.Listener {


            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                mediaSession.setPlaybackState(
                    BackgroundPlayerStates().setStateToPlaying(
                        exoPlayer?.currentPosition!!,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
                mediaSession.setMetadata(
                    mediaMetaDetails(
                        exoPlayer?.currentMediaItem?.mediaMetadata?.title!!.toString(),
                        exoPlayer?.currentMediaItem?.requestMetadata?.mediaUri.toString(),
                        exoPlayer?.duration!!
                    )
                )
                createMediaNotification()
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                val currentPosition = newPosition.positionMs

                mediaSession.setPlaybackState(
                    BackgroundPlayerStates().setStateToPlaying(
                        currentPosition,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
            }



            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                if (playbackState == Player.STATE_ENDED) {
                    mediaSession.setPlaybackState(
                        BackgroundPlayerStates().setStateToPaused(
                            exoPlayer?.currentPosition!!,
                            exoPlayer?.shuffleModeEnabled!!
                        )
                    )
                }
                if (playbackState == Player.STATE_BUFFERING){
                    mediaSession.setPlaybackState(
                        BackgroundPlayerStates().setStateToLoading(
                            exoPlayer?.currentPosition!!,
                            exoPlayer?.shuffleModeEnabled!!
                        )
                    )
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (playWhenReady) {

                    requestAudioFocusForAudioService()
                    mediaSession.setPlaybackState(
                        BackgroundPlayerStates().setStateToPlaying(
                            exoPlayer?.currentPosition!!,
                            exoPlayer?.shuffleModeEnabled!!
                        )
                    )
                    mediaSession.setMetadata(
                        mediaMetaDetails(
                            exoPlayer?.currentMediaItem?.mediaMetadata?.title.toString(),
                            mediaUri,
                            exoPlayer?.duration!!
                        )
                    )

                } else {
                    releaseAudioFocusForAudioService()
                    mediaSession.setPlaybackState(
                        BackgroundPlayerStates().setStateToPaused(
                            exoPlayer?.currentPosition!!,
                            exoPlayer?.shuffleModeEnabled!!
                        )
                    )



                    mediaSession.setMetadata(
                        mediaMetaDetails(
                            exoPlayer?.currentMediaItem?.mediaMetadata?.title.toString(),
                            mediaUri,
                            exoPlayer?.duration!!
                        )
                    )
                }

            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                mediaSession.setPlaybackState(
                    BackgroundPlayerStates().setStateToLoading(
                        error.timestampMs,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying){
                    mediaSession.setMetadata(
                        mediaMetaDetails(
                            exoPlayer?.currentMediaItem?.mediaMetadata?.title!!.toString(),
                            exoPlayer?.currentMediaItem?.mediaId!!,
                            exoPlayer?.duration!!
                        )
                    )

                    // Update playback state
                    mediaSession.setPlaybackState(
                        BackgroundPlayerStates().setStateToPlaying(
                            exoPlayer?.currentPosition!!,
                            exoPlayer?.shuffleModeEnabled!!
                        )
                    )
                }
                if (!isPlaying && exoPlayer?.isLoading!!) {
                    mediaSession.setPlaybackState(
                        BackgroundPlayerStates().setStateToLoading(
                            exoPlayer?.currentPosition!!,
                            exoPlayer?.shuffleModeEnabled!!
                        )
                    )
                }
                if (!isPlaying && !exoPlayer?.isLoading!!){

                    mediaSession.setPlaybackState(
                        BackgroundPlayerStates().setStateToPaused(
                            exoPlayer?.currentPosition!!,
                            exoPlayer?.shuffleModeEnabled!!
                        )
                    )
                }
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                super.onIsLoadingChanged(isLoading)
                if (!isLoading){
                    mediaSession.setMetadata(
                        mediaMetaDetails(
                            exoPlayer?.currentMediaItem?.mediaMetadata?.title!!.toString(),
                            exoPlayer?.currentMediaItem?.mediaId!!,
                            exoPlayer?.duration!!
                        )
                    )

                    mediaSession.setPlaybackState(
                        BackgroundPlayerStates().setStateToPlaying(
                            exoPlayer?.currentPosition!!,
                            exoPlayer?.shuffleModeEnabled!!
                        )
                    )
                }
                else{
                    mediaSession.setPlaybackState(
                        BackgroundPlayerStates().setStateToLoading(
                            exoPlayer?.currentPosition!!,
                            exoPlayer?.shuffleModeEnabled!!
                        )
                    )
                }
            }
        }
        )


        mediaSession.setCallback(
            MyMediaSessionCallBack()
        )
        mediaSession.setMetadata(
            mediaMetaDetails(
                exoPlayer?.currentMediaItem?.mediaMetadata?.title.toString(),
                mediaUri,
                exoPlayer?.duration!!
            )
        )
        when (intent?.action) {

            ACTION_START -> {

                exoPlayer?.prepare()
                exoPlayer?.play()

                exoPlayer?.setMediaItems(
                    fetchDataFromFolder()
                )

                mediaSession.setPlaybackState(
                    BackgroundPlayerStates().setStateToPlaying(
                        exoPlayer?.currentPosition!!,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
                mediaSession.setMetadata(
                    mediaMetaDetails(
                        exoPlayer?.currentMediaItem?.mediaMetadata?.title.toString(),
                        mediaUri,
                        exoPlayer?.duration!!
                    )
                )
            }

        }




        MediaButtonReceiver.handleIntent(mediaSession, intent)
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
            ).apply {
                enableLights(false)
            }

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
            .setLargeIcon(
                BitmapFactory.decodeResource(resources, drawable.music_note_24dp)
            )
            .setStyle(mediaStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

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

    private fun mediaMetaDetails(
        title: String,
        mediaUri: String,
        duration: Long
    ): MediaMetadataCompat {

        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "unknown album")

            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,
                "unknown album"
            )
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)

            .putBitmap(
                    MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                    BitmapFactory.decodeResource(resources, drawable.music_note_24dp)
            )
        return metadata.build()
    }


    inner class MyMediaSessionCallBack: MediaSessionCompat.Callback() {

        override fun onSetRepeatMode(repeatMode: Int) {
            super.onSetRepeatMode(repeatMode)
            exoPlayer?.repeatMode = repeatMode
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            exoPlayer?.seekTo(pos)
        }

        override fun onPlay() {
            super.onPlay()

            requestAudioFocusForAudioService()
            exoPlayer?.play()
            mediaSession.setPlaybackState(
                BackgroundPlayerStates().setStateToPlaying(
                    exoPlayer?.currentPosition!!,
                    exoPlayer?.shuffleModeEnabled!!
                )
            )
        }

        override fun onPause() {
            super.onPause()
            println("here is it from1")
            releaseAudioFocusForAudioService()
            exoPlayer?.pause()
            mediaSession.setPlaybackState(
                BackgroundPlayerStates().setStateToPaused(
                    exoPlayer?.currentPosition!!,
                    exoPlayer?.shuffleModeEnabled!!
                )
            )
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            exoPlayer?.seekToPrevious()
            mediaSession.setMetadata(
            mediaMetaDetails(
                exoPlayer?.currentMediaItem?.mediaMetadata?.title!!.toString(),
                exoPlayer?.currentMediaItem?.mediaId!!,
                exoPlayer?.duration!!
            )
            )
            mediaSession.setPlaybackState(
                BackgroundPlayerStates().setStateToLoading(
                    exoPlayer?.currentPosition!!,
                    exoPlayer?.shuffleModeEnabled!!
                )
            )
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            exoPlayer?.seekToNext()
//            Handler(Looper.getMainLooper()).postDelayed({
                // Update metadata and playback state after the delay

                // Update media session metadata
                mediaSession.setMetadata(
                    mediaMetaDetails(
                        exoPlayer?.currentMediaItem?.mediaMetadata?.title!!.toString(),
                        exoPlayer?.currentMediaItem?.mediaId!!,
                        exoPlayer?.duration!!
                    )
                )

                // Update playback state
                mediaSession.setPlaybackState(
                    BackgroundPlayerStates().setStateToLoading(
                        exoPlayer?.currentPosition!!,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
//            }, 1500)
        }


        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
            val actions = action.toString()
            if (actions == SET_SHUFFLE_MODE){
                exoPlayer?.shuffleModeEnabled = exoPlayer?.shuffleModeEnabled != true

                mediaSession.setPlaybackState(
                    BackgroundPlayerStates().setStateToPlaying(
                        exoPlayer?.currentPosition!!,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
            }
            if (action == ACTION_KILL){
                exoPlayer?.let {
                    it.stop()
                    it.release()
                }
                mediaSession.release()
                exoPlayer?.release()
                stopSelf()
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.cancel(1)

            }
        }


        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            mediaButtonEvent?.let {

                if (it.action == Intent.ACTION_MEDIA_BUTTON) {
                    // Extract the key event from the intent
                    val keyEvent: KeyEvent? = it.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
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


    private fun fetchDataFromFolder(): MutableList<MediaItem> {
        val fileLists = mutableListOf<MediaItem>().apply {
            clear()
        }



        val pathOfVideos = File(
            PathSaver()
                .getAudioDownloadPath(this)
        )
        if (pathOfVideos.exists()) {
            val fileNames = arrayOfNulls<String>(pathOfVideos.listFiles()!!.size)
            val pathOfVideosUris = arrayOfNulls<Uri?>(pathOfVideos.listFiles()!!.size)
            pathOfVideos.listFiles()!!.mapIndexed { index, item ->
                fileNames[index] = item?.name
                pathOfVideosUris[index] = item?.toUri()

            }
            fileNames.zip(pathOfVideosUris).forEach { (fileName, videoUri) ->
                if (videoUri != null && fileName != null) {
                    val exoMetadata = MediaMetadata.Builder()
                        .setTitle(fileName)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                        .build()
                    fileLists.add(
                        MediaItem.Builder()
                            .setMediaId(videoUri.toString())
                            .setUri(videoUri)
                            .setMediaMetadata(exoMetadata)
                            .build()
                    )
                }
            }
        }
        return fileLists

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
}

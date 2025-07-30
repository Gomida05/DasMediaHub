package com.das.mediaHub.services

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
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.text.format.Formatter.formatFileSize
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.das.mediaHub.MainActivity
import com.das.mediaHub.R.drawable
import com.das.mediaHub.data.databased.PathSaver.getAudioDownloadPath
import com.das.mediaHub.data.constants.Action.ACTION_KILL
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.constants.Playback.SET_SHUFFLE_MODE
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.das.mediaHub.data.YouTuber.formatDateFromLong
import com.das.mediaHub.mediacontroller.BackgroundPlayerStates.setStateToPaused
import com.das.mediaHub.mediacontroller.BackgroundPlayerStates.setStateToLoading
import com.das.mediaHub.mediacontroller.BackgroundPlayerStates.setStateToPlaying
import com.das.mediaHub.data.constants.Notifications.BACKGROUND_GROUND_PLAYER_NOTIFICATION
import com.das.mediaHub.data.YouTuber.mediaItems
import java.io.File


class BackGroundPlayer: Service() {

    private val channelId = "MusicPlayerNotification"
    private var exoPlayer: ExoPlayer? = null
    lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioManager: AudioManager
    private var mediaId: Int = 0
    private var audioFocusRequest: AudioFocusRequest? = null


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(AudioManager::class.java)
        exoPlayer = ExoPlayer.Builder(this).build()


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaSession = MediaSessionCompat(this, "BackGroundPlayer").apply {
            isActive = true
            @Suppress("DEPRECATION")
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            setMediaButtonReceiver(
                PendingIntent.getBroadcast(
                    this@BackGroundPlayer, 0,
                    Intent(Intent.ACTION_MEDIA_BUTTON),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

        if (intent != null) {
            mediaId = intent.getIntExtra("media_id", 0)
        }


















        mediaSession.setCallback(
            MyMediaSessionCallBack()
        )
        when (intent?.action) {

            ACTION_START -> {

                if (exoPlayer?.isPlaying!!) {
                    if (exoPlayer?.currentMediaItemIndex != mediaId) {
                        exoPlayer?.apply {
                            pause()
                            seekTo(mediaId, 0)
                        }
                        exoPlayer?.addListener(
                            ExoPlayerListener(
                                exoPlayer?.currentMediaItem?.mediaId!!
                            )
                        )
                        mediaSession.setPlaybackState(
                            setStateToPlaying(
                                exoPlayer?.currentPosition!!,
                                exoPlayer?.shuffleModeEnabled!!
                            )
                        )
                        mediaSession.setMetadata(
                            mediaMetaDetails(
                                exoPlayer?.currentMediaItem?.mediaMetadata?.title.toString(),
                                exoPlayer?.currentMediaItem?.mediaId!!,
                                exoPlayer?.duration!!
                            )
                        )
                    }
                } else if (exoPlayer?.currentMediaItem == null) {
                    val listMediaItems = mediaItems.ifEmpty { fetchDataFromFolder() }
                    exoPlayer?.apply {
                        setMediaItems(listMediaItems)
                        seekTo(mediaId, 0)
                        prepare()
                        play()
                    }


                    exoPlayer?.addListener(
                        ExoPlayerListener(
                            exoPlayer?.currentMediaItem?.mediaId!!
                        )
                    )
                    mediaSession.setPlaybackState(
                        setStateToPlaying(
                            exoPlayer?.currentPosition!!,
                            exoPlayer?.shuffleModeEnabled!!
                        )
                    )
                    mediaSession.setMetadata(
                        mediaMetaDetails(
                            exoPlayer?.currentMediaItem?.mediaMetadata?.title.toString(),
                            exoPlayer?.currentMediaItem?.mediaId!!,
                            exoPlayer?.duration!!
                        )
                    )


                }
            }

        }




        MediaButtonReceiver.handleIntent(mediaSession, intent)
        val notifications = createMediaNotification()
        startForeground(95, notifications)
        return START_STICKY
    }






    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Local Music Player",
                NotificationManager.IMPORTANCE_LOW
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
                action = BACKGROUND_GROUND_PLAYER_NOTIFICATION
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
            .setDeleteIntent(deletePendingIntent)
            .build()


        getSystemService(NotificationManager::class.java).notify(95, notification)
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



    @Suppress("DEPRECATION")
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

    @Suppress("DEPRECATION")
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

//            .putBitmap(
            .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
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
                setStateToPlaying(
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
                setStateToPaused(
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
                setStateToLoading(
                    exoPlayer?.currentPosition!!,
                    exoPlayer?.shuffleModeEnabled!!
                )
            )
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            exoPlayer?.seekToNext()
                mediaSession.setMetadata(
                    mediaMetaDetails(
                        exoPlayer?.currentMediaItem?.mediaMetadata?.title!!.toString(),
                        exoPlayer?.currentMediaItem?.mediaId!!,
                        exoPlayer?.duration!!
                    )
                )

                // Update playback state
                mediaSession.setPlaybackState(
                    setStateToLoading(
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
                    setStateToPlaying(
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

                    @Suppress("DEPRECATION")
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

        override fun onStop() {
            super.onStop()
            mediaSession.release()
        }


    }


    private fun fetchDataFromFolder(): MutableList<MediaItem> {

        val fileLists = mutableListOf<MediaItem>()
        val pathOfMusics = File(
            getAudioDownloadPath(this)
        )

        if (pathOfMusics.exists()) {
            pathOfMusics.listFiles()?.forEach { file ->
                val lastModified = file.lastModified()
                val formattedDate = formatDateFromLong(lastModified)
                val fileSizeFormatted = formatFileSize(this, file.length())
                val mediaMetaData = MediaMetadata.Builder()
                    .setTitle(file.name.removeSuffix(".mp3"))
                    .setDescription(formattedDate)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .build()
                fileLists.add(
                    MediaItem.Builder()
                        .setMediaId(file.toUri().toString())
                        .setUri(file.toUri())
                        .setMediaMetadata(mediaMetaData)
                        .setTag(fileSizeFormatted)
                        .build()
                )


            }
        }
        return fileLists

    }





    private inner class ExoPlayerListener(private val mediaUri: String): Player.Listener {


        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
            mediaSession.setPlaybackState(
                setStateToPlaying(
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
                setStateToPlaying(
                    currentPosition,
                    exoPlayer?.shuffleModeEnabled!!
                )
            )
        }



        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            if (playbackState == Player.STATE_ENDED) {
                mediaSession.setPlaybackState(
                    setStateToPaused(
                        exoPlayer?.currentPosition!!,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
            }
            if (playbackState == Player.STATE_BUFFERING){
                mediaSession.setPlaybackState(
                    setStateToLoading(
                        exoPlayer?.currentPosition!!,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
            }
            createMediaNotification()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)
            if (playWhenReady) {

                requestAudioFocusForAudioService()
                mediaSession.setPlaybackState(
                    setStateToPlaying(
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
                    setStateToPaused(
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
            createMediaNotification()

        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            mediaSession.setPlaybackState(
                setStateToLoading(
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
                    setStateToPlaying(
                        exoPlayer?.currentPosition!!,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
            }
            if (!isPlaying && exoPlayer?.isLoading!!) {
                mediaSession.setPlaybackState(
                    setStateToLoading(
                        exoPlayer?.currentPosition!!,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
            }
            if (!isPlaying && !exoPlayer?.isLoading!!){

                mediaSession.setPlaybackState(
                    setStateToPaused(
                        exoPlayer?.currentPosition!!,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
            }
            createMediaNotification()
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
                    setStateToPlaying(
                        exoPlayer?.currentPosition!!,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
            }
            else{
                mediaSession.setPlaybackState(
                    setStateToLoading(
                        exoPlayer?.currentPosition!!,
                        exoPlayer?.shuffleModeEnabled!!
                    )
                )
            }
            createMediaNotification()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!exoPlayer?.isPlaying!!){
            exoPlayer?.release()
            mediaSession.release()
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession.release()
        exoPlayer?.release()
        super.onDestroy()
    }
    override fun stopService(name: Intent?): Boolean {
        mediaSession.release()
        exoPlayer?.release()
        return super.stopService(name)
    }
}


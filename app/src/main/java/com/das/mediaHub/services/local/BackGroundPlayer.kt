package com.das.mediaHub.services.local

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.text.format.Formatter
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.NotificationListener
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.local.PathPreferences
import com.das.mediaHub.python.YouTuber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@SuppressLint("UnsafeOptInUsageError")
class BackGroundPlayer: Service() {

    private val exoPlayer by lazy {
        ExoPlayer.Builder(this)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        1_000,
                        10_000,
                        1_000,
                        1_000
                    )
                    .build()
            )
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
    }


    private val mediaSession by lazy {
        MediaSession.Builder(this, exoPlayer).build()
    }

    private val playerNotificationManager by lazy {
        PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setMediaDescriptionAdapter(DescriptionAdapter(this))
            .setNotificationListener(notificationListener)
            .build()
            .apply {
                setPlayer(exoPlayer)
                setMediaSessionToken(mediaSession.platformToken)
            }
    }
    private val serviceScope = CoroutineScope(
        Dispatchers.Main + SupervisorJob()
    )

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            val items = withContext(Dispatchers.IO) {
                fetchDataFromFolder()
            }


            if (items.isNotEmpty() && exoPlayer.mediaItemCount == 0) {
                exoPlayer.setMediaItems(items)
                exoPlayer.prepare()
            }

        }
        playerNotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val mediaId = intent?.getIntExtra("media_id", 0) ?: 0

        when (intent?.action) {
            ACTION_START -> {
                if (exoPlayer.isPlaying) {
                    exoPlayer.seekTo(mediaId, 0)
                    exoPlayer.prepare()
                }
                exoPlayer.play()
            }
        }

        return START_STICKY
    }






    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun fetchDataFromFolder(): List<MediaItem> {
        val musicDir = File(PathPreferences.getAudioPath(this))

        if (!musicDir.exists() || !musicDir.isDirectory) {
            Log.w("fetchDataFromFolder", "Invalid music directory")
            return emptyList()
        }

        val files = musicDir.listFiles { file ->
            file.isFile && file.extension.equals("mp3", ignoreCase = true)
        } ?: return emptyList()

        return files.mapNotNull { file ->
            try {
                val formattedDate =
                    YouTuber.formatDateFromLong(file.lastModified())

                val fileSizeFormatted =
                    Formatter.formatFileSize(this, file.length())

                val metadata = MediaMetadata.Builder()
                    .setTitle(file.nameWithoutExtension)
                    .setArtist("Unknown artist")
                    .setAlbumTitle("Unknown album")
                    .setDescription(formattedDate)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .build()

                MediaItem.Builder()
                    .setMediaId(file.toUri().toString())
                    .setUri(file.toUri())
                    .setMediaMetadata(metadata)
                    .setTag(fileSizeFormatted)
                    .build()

            } catch (e: Exception) {
                Log.e(
                    "fetchDataFromFolder",
                    "Skipping unreadable file: ${file.name}",
                    e
                )
                null // skip bad file, continue scanning
            }
        }
    }

    private val notificationListener =
        object : NotificationListener {

            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                if (ongoing) {
                    startForeground(notificationId, notification)
                }
            }

            override fun onNotificationCancelled(
                notificationId: Int,
                dismissedByUser: Boolean
            ) {
                stopSelf()
            }
        }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!exoPlayer.isPlaying){
            stopSelf()
        }
    }

    override fun onDestroy() {
        playerNotificationManager.setPlayer(null)
        mediaSession.release()
        exoPlayer.release()
        super.onDestroy()
    }

    private companion object {
        const val CHANNEL_ID = "MusicPlayerNotification"
        const val NOTIFICATION_ID = 95
    }
}
package com.das.forui.services

import android.app.PendingIntent
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.media.session.MediaSessionCompat
import com.das.forui.MediaPlayerNotificationManager
import com.das.forui.NotificationListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject


private const val SERVICE_TAG = "MusicService"
class MyMediaService: MediaBrowserService() {


    private lateinit var notificationManager: MediaPlayerNotificationManager
    var isForeGroundService = false
    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSessionCompact: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    override fun onCreate() {
        super.onCreate()

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        mediaSessionCompact = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        sessionToken = mediaSessionCompact.sessionToken as MediaSession.Token?

        notificationManager = MediaPlayerNotificationManager(
            this,
            mediaSessionCompact.sessionToken,
            NotificationListener(this)
        ) {}
//        notificationManager.showNotification(exoPlayer)

        mediaSessionConnector = MediaSessionConnector(mediaSessionCompact)

        mediaSessionConnector.setPlayer(exoPlayer)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
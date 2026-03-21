package com.das.mediaHub

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TEXT
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.das.mediaHub.NavScreens.AboutDasMediaHub
import com.das.mediaHub.NavScreens.Downloaded
import com.das.mediaHub.NavScreens.DownloadsPage
import com.das.mediaHub.NavScreens.ExoPlayerUI
import com.das.mediaHub.NavScreens.FeedbackScreen
import com.das.mediaHub.NavScreens.Home
import com.das.mediaHub.NavScreens.Instagram
import com.das.mediaHub.NavScreens.RecentlyWatched
import com.das.mediaHub.NavScreens.Saved
import com.das.mediaHub.NavScreens.Searcher
import com.das.mediaHub.NavScreens.Setting
import com.das.mediaHub.NavScreens.TikTok
import com.das.mediaHub.NavScreens.VideoViewer
import com.das.mediaHub.OnLaunchComponents.BottomNavItems
import com.das.mediaHub.OnLaunchComponents.newTextIntent
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.constants.DownloadConstants.DOWNLOAD_FINISHED
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.services.local.BackGroundPlayer
import com.das.mediaHub.ui.TopPopupNotification.TopPopupNotification
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.downloaded.DownloadedComposable
import com.das.mediaHub.ui.downloads.DownloadingComposable
import com.das.mediaHub.ui.home.HomePageComposable
import com.das.mediaHub.ui.home.PageNotFound
import com.das.mediaHub.ui.instagram.InstagramComposable
import com.das.mediaHub.ui.players.videoPlayer.OnlineVideoPlayer
import com.das.mediaHub.ui.players.videoPlayerLocally.LocalVideoPlayer
import com.das.mediaHub.ui.result.ResultViewerPage
import com.das.mediaHub.ui.search.SearchPageCompose
import com.das.mediaHub.ui.settings.AboutDasMediaHub
import com.das.mediaHub.ui.settings.FeedbackComposable
import com.das.mediaHub.ui.settings.SettingsComposable
import com.das.mediaHub.ui.settings.watch_later.WatchLaterComposable
import com.das.mediaHub.ui.theme.DasMediaHubTheme
import com.das.mediaHub.ui.tiktok.TikTokComposable
import com.das.mediaHub.ui.watchedVideos.WatchedVideosComposable
import java.io.File


class MainActivity : ComponentActivity() {

    private var pendingIntent by mutableStateOf<Intent?>(null)


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        onNewIntent(intent)
        setContent {
            DasMediaHubTheme {
                MainLauncherPageComposable()
            }
        }
    }



    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingIntent = intent
    }

    @Composable
    private fun MainLauncherPageComposable() {

        val backStack = rememberNavBackStack(Home)


        LaunchedEffect(pendingIntent) {
            pendingIntent?.let {
                backStack.handleNavIntent(it)
                pendingIntent = null
            }
        }


        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier
                .fillMaxSize(),
            bottomBar = {
                BottomNavItems(backStack)
            },
        ) { paddingValues ->

            showNotificationDialog?.let {
                Box(
                    Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                        .zIndex(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    it.TopPopupNotification {
                        showNotificationDialog = null
                    }
                }
            }

            NavDisplay(
                backStack = backStack,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) { key ->

                when (key) {

                    is Home -> {
                        NavEntry(key = key) {
                            backStack.HomePageComposable()
                        }
                    }

                    is RecentlyWatched -> {
                        NavEntry(key = key) {
                            WatchedVideosComposable(backStack)
                        }
                    }

                    is Setting -> {
                        NavEntry(key = key) {
                            SettingsComposable {
                                backStack.add(it)
                            }

                        }
                    }

                    is VideoViewer -> {
                        NavEntry(key = key) {
                            OnlineVideoPlayer(backStack, key.data)
                        }
                    }

                    is NavScreens.ResultViewerPage -> {
                        NavEntry(key = key) {
                            ResultViewerPage(
                                backStack,
                                key.value
                            )
                        }
                    }

                    is DownloadsPage -> {
                        NavEntry(key = key) {
                            DownloadingComposable(backStack)
                        }
                    }

                    is Downloaded -> {
                        NavEntry(key = key) {
                            DownloadedComposable(backStack)
                        }
                    }

                    is Searcher -> {
                        NavEntry(key = key) {
                            SearchPageCompose(backStack, key.text)
                        }
                    }

                    is ExoPlayerUI -> {
                        NavEntry(key = key) {
                            LocalVideoPlayer(videoUri = key.uri)
                        }
                    }

                    is Saved -> {
                        NavEntry(key = key) {
                            WatchLaterComposable(backStack)
                        }
                    }

                    is TikTok -> {
                        NavEntry(key = key) {
                            backStack.TikTokComposable()
                        }
                    }

                    is Instagram -> {
                        NavEntry(key = key) {
                            backStack.InstagramComposable()
                        }
                    }

                    is FeedbackScreen -> {
                        NavEntry(key = key) {
                            FeedbackComposable(backStack = backStack)
                        }
                    }

                    is AboutDasMediaHub -> {
                        NavEntry(key = key) {
                            AboutDasMediaHub(backStack = backStack)
                        }
                    }

                    else -> {
                        NavEntry(key = key) {
                            PageNotFound(backStack = backStack)
                        }
                    }
                }
            }
        }
    }





    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (PIP.canEnterPipMode.value) {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
            )
        }

    }

    private fun NavBackStack<NavKey>.handleNavIntent(intent: Intent) {
        when (intent.action) {

            Intent.ACTION_SEND -> {
                val type = intent.type.orEmpty()
                when {
                    type.startsWith("text/") ->
                        newTextIntent(intent.getStringExtra(EXTRA_TEXT).orEmpty())

                    type.startsWith("video/") ->
                        intent.newReceivedMediaTypeVideo(this)

                    type.startsWith("audio/") ->
                        intent.newReceivedMediaTypeAudio()
                }
            }

            Intent.ACTION_VIEW -> {
                intent.data?.newMediaIntent(this)
            }

            DOWNLOAD_FINISHED -> {
                intent.getStringExtra("apk_path")?.let {
                    File(it).requestToInstall()
                }
            }

            Intent.ACTION_APPLICATION_PREFERENCES -> {
                add(Setting)
            }
        }
    }


    private fun File.requestToInstall() {

        val apkUri = FileProvider.getUriForFile(
            this@MainActivity,
            "${this@MainActivity.packageName}.file-provider",
            this
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivity(installIntent)
    }

    override fun onStart() {
        super.onStart()

        if (SDK_INT >= TIRAMISU) {
            if (this.checkSelfPermission(
                    arrayOf(
                        POST_NOTIFICATIONS,
                        READ_MEDIA_VIDEO,
                        READ_MEDIA_AUDIO
                    ).toString())
                != PackageManager.PERMISSION_GRANTED
            ) {
                this.requestPermissions(
                    arrayOf(
                        POST_NOTIFICATIONS,
                        READ_MEDIA_VIDEO,
                        READ_MEDIA_AUDIO
                    ), 1)
            }
        }
    }



    private fun Intent.newReceivedMediaTypeVideo(backStack: NavBackStack<NavKey>){

        @Suppress("DEPRECATION")
        val videoUri = if (SDK_INT >= TIRAMISU) getParcelableExtra(EXTRA_STREAM, Uri::class.java)
        else getParcelableExtra(EXTRA_STREAM)

        backStack.add(ExoPlayerUI(videoUri.toString()))
    }

    private fun Intent.newReceivedMediaTypeAudio(){
        @Suppress("DEPRECATION")
        val audioUri = if (SDK_INT >= TIRAMISU) getParcelableExtra(EXTRA_STREAM, Uri::class.java)
        else getParcelableExtra(EXTRA_STREAM)

        val playIntent = Intent(this@MainActivity, BackGroundPlayer::class.java).apply {
            action = ACTION_START
            putExtra("media_id", audioUri?.path)
            putExtra("media_url", audioUri?.path)
            putExtra("title", title)
        }
        startService(playIntent)
    }

    private fun Uri.newMediaIntent(
        backStack: NavBackStack<NavKey>
    ) {
        val mimeType = contentResolver.getType(this) ?: ""

        if (mimeType.startsWith("video/")) {
            backStack.add(ExoPlayerUI(toString()))
        } else if (mimeType.startsWith("audio/")) {


            val playIntent = Intent(this@MainActivity, BackGroundPlayer::class.java).apply {
                action = ACTION_START
                putExtra("media_id", path)
                putExtra("media_url", path)
                putExtra("title", title)
            }
            startService(playIntent)

        } else {
            showNotificationDialog = TopPopUp(
                message = "Unsupported media type",
                icon = Icons.Default.Info
            )
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        pendingIntent = null
    }

}

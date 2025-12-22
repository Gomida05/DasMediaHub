package com.das.mediaHub

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TEXT
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.das.mediaHub.python.YouTuber.youtubeExtractor
import com.das.mediaHub.python.YouTuber.isValidYoutubeURL
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.das.mediaHub.python.YouTuber.getAudioStreamUrl
import com.das.mediaHub.python.YouTuber.getVideoStreamUrl
import com.das.mediaHub.downloader.DownloaderClass
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.python.YouTuber.extractPlaylistId
import com.das.mediaHub.python.YouTuber.getPlayListStreamUrl
import com.das.mediaHub.python.YouTuber.isValidYouTubePlaylistUrl
import com.das.mediaHub.services.BackGroundPlayer
import com.das.mediaHub.ui.downloads.DownloadsComposable
import com.das.mediaHub.ui.home.HomePageComposable
import com.das.mediaHub.ui.result.ResultViewerPage
import com.das.mediaHub.ui.search.SearchPageCompose
import com.das.mediaHub.ui.settings.watch_later.WatchLaterComposable
import com.das.mediaHub.ui.settings.SettingsComposable
import com.das.mediaHub.ui.settings.userSettings.UserSettingComposable
import com.das.mediaHub.ui.players.videoPlayerLocally.LocalVideoPlayer
import com.das.mediaHub.ui.auth.LoginPage
import com.das.mediaHub.ui.auth.signup.SignUpPage
import com.das.mediaHub.ui.players.videoPlayer.OnlineVideoPlayer
import com.das.mediaHub.ui.watchedVideos.WatchedVideosComposable
import com.das.mediaHub.NavScreens.*
import com.das.mediaHub.OnLaunchComponents.BottomNavItems
import com.das.mediaHub.PIP.shouldEnterPipMode
import com.das.mediaHub.WakeLockHelper.releaseWakeLock
import com.das.mediaHub.auth.MyFirebase.rememberFirebaseUser
import com.das.mediaHub.data.constants.DownloadConstants.DOWNLOAD_FINISHED
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.data.model.searcher.Video
import com.das.mediaHub.ui.theme.CustomTheme
import com.das.mediaHub.ui.TopPopupNotification.TopPopupNotification
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.auth.AccountSettingsPage
import com.das.mediaHub.ui.auth.ChangePasswordPage
import com.das.mediaHub.ui.settings.FeedbackComposable
import com.das.mediaHub.ui.welcome.WelcomePage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : ComponentActivity() {

    private val intentListeners = mutableSetOf<(Intent) -> Unit>()

    private var intentListener: ((Intent) -> Unit)? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CustomTheme {
                MainLauncherPageComposable()
            }
        }
    }



    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentListeners.forEach { it(intent) }
    }

    private fun registerIntentListener(listener: (Intent) -> Unit) {
        intentListeners.add(listener)
    }

    private fun unregisterIntentListener(listener: (Intent) -> Unit) {
        intentListeners.remove(listener)
    }



    @Composable
    private fun MainLauncherPageComposable() {

        val backStack = rememberNavBackStack(Home)

        val currentRoute = backStack.lastOrNull()

        LaunchedEffect(Unit) {
            intent?.let {
                backStack.listenNewIntent(it)
            }
        }

        DisposableEffect(Unit) {
            val listener: (Intent) -> Unit = {
                backStack.listenNewIntent(it)
            }
            registerIntentListener(listener)
            onDispose {
                unregisterIntentListener(listener)
            }
        }


        val auth = Firebase.auth
        val currentUser = auth.rememberFirebaseUser()

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier
                .fillMaxSize(),
            bottomBar = {
                BottomNavItems(currentRoute, backStack)
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
                            backStack.SettingsComposable {
                                showNotificationDialog = it
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

                    is Downloads -> {
                        NavEntry(key = key) {
                            DownloadsComposable(backStack)
                        }
                    }

                    is Searcher -> {
                        NavEntry(key = key) {
                            SearchPageCompose(backStack, key.text)
                        }
                    }

                    is UserSettings -> {
                        NavEntry(key = key) {
                            UserSettingComposable(backStack)
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

                    is SignInPage -> {
                        NavEntry(key = key) {
                            LoginPage(backStack)
                        }
                    }

                    is AccountSetting -> {
                        NavEntry(key = key) {
                            if (currentUser != null) {
                                AccountSettingsPage(backStack, currentUser) {
                                    auth.signOut()
                                }
                            } else {
                                LoginPage(backStack)
                            }
                        }
                    }

                    is ChangePassword -> {
                        NavEntry(key = key) {
                            ChangePasswordPage(backStack, auth)
                        }
                    }

                    is WelcomePage -> {
                        NavEntry(key = key) {
                            WelcomePage(backStack) {
                                if (auth.currentUser == null) {
                                    auth.signInAnonymously()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Log.d(
                                                    "Auth",
                                                    "Signed in anonymously as ${auth.currentUser?.uid}"
                                                )
                                            } else {
                                                Log.e(
                                                    "Auth",
                                                    "Anonymous sign-in failed",
                                                    task.exception
                                                )
                                            }
                                        }
                                }
                                backStack.add(Home)
                                backStack.removeLastOrNull()
                            }
                        }
                    }

                    is SignUpPage -> {
                        NavEntry(key = key) {
                            SignUpPage(backStack)
                        }
                    }

                    is FeedbackScreen -> {
                        NavEntry(key = key) {
                            FeedbackComposable()
                        }
                    }

                    else -> {
                        NavEntry(key = key) {

                        }
                    }
                }
            }
        }
    }





    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (shouldEnterPipMode.value) {

            val params = PictureInPictureParams.Builder()
                .apply {
                    setAspectRatio(Rational(16, 9))
                    if (SDK_INT >= VERSION_CODES.S) {
                        setSeamlessResizeEnabled(true)
                    }
                }
                .build()
            enterPictureInPictureMode(params)
        }
    }


    private fun NavBackStack<NavKey>.listenNewIntent(
        newIntent: Intent
    ) {
        if (newIntent.action == Intent.ACTION_SEND) {
            val intentType = newIntent.type.toString()

            if (intentType.startsWith("text/")) {
                newTextIntent(
                    sharedText = newIntent.getStringExtra(EXTRA_TEXT).toString()
                )
            } else if (intentType.startsWith("video/")) {
                newIntent.newReceivedMediaTypeVideo(this)
            } else if (intentType.startsWith("audio/")) {
                newIntent.newReceivedMediaTypeAudio()
            }
        } else if (newIntent.action == Intent.ACTION_VIEW) {
            newIntent.data?.newMediaIntent(backStack = this)

        } else if (newIntent.action == DOWNLOAD_FINISHED) {
            val apkPath = newIntent.getStringExtra("apk_path") ?: return
            File(apkPath)
                .requestToInstall()
        } else if (newIntent.action == Intent.ACTION_APPLICATION_PREFERENCES) {
            add(Setting)
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
            showDialogs("Unsupported media type")
        }
    }
    private fun NavBackStack<NavKey>.newTextIntent(
        sharedText: String
    ) {
        if (sharedText.isValidYoutubeURL()) {
            val videoId = sharedText.youtubeExtractor()
            add(
                VideoViewer(
                    Video(id = videoId.toString())
                )
            )

        } else if (sharedText.isValidYouTubePlaylistUrl()) {

            add(
                VideoViewer(
                    Video(id = extractPlaylistId(sharedText).toString())
                )
            )

        } else if (sharedText.startsWith("DownloadsPageFr")) {
            add(Downloads)
        } else {
            add(Searcher(sharedText))
        }
    }


    fun startDownloadingVideo(videoId: String, title: String){
        val downloaderClass = DownloaderClass(this)
        showNotificationDialog = TopPopUp(
            message = "Start downloading $title now",
            icon = Icons.Default.Downloading,
            loading = true
        )
        lifecycleScope.launch {
            getVideoStreamUrl(videoId,
                onSuccess = {
                    downloaderClass.downloadVideo(it, title)
                },
                onFailure = {
                    alertUserError(this@MainActivity, it)
                    showDialogs(it)
                }
            )
        }
    }

    fun startPlayListDownload(
        playListUrl: String
    ){
        val downloaderClass = DownloaderClass(this)

        lifecycleScope.launch {
            try {
                getPlayListStreamUrl(
                    playListUrl,
                    onSuccess = { playListName, videoList ->
                        for (i in videoList) {
                            downloaderClass.downloadVideosPlayList(
                                i.url,
                                playListName,
                                i.title
                            )
                        }
                    },
                    onFailure = {
                        alertUserError(this@MainActivity, it)
                        Log.e("There is an error ", it)
                    }
                )
            } catch (e: Exception) {
                Log.e("Playlist", "Error getting playlist", e)
            }
        }
    }



    fun startDownloadingAudio(videoId: String, title: String){
        val downloaderClass = DownloaderClass(this)
        showNotificationDialog = TopPopUp(
            message = "Start downloading $title now",
            icon = Icons.Default.Downloading,
            loading = true
        )
        lifecycleScope.launch {
            getAudioStreamUrl(videoId,
                onSuccess = {
                    downloaderClass.downloadMusic(it, title)
                },
                onFailure = {
                    showDialogs(it)
                }
            )
        }
    }


    fun alertUserError(context: Context, message: String?) {

        val notification = NotificationCompat.Builder(context, "error_searching")
            .setContentTitle("Found an error")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher_ofme)
            .setOngoing(true)
            .setAutoCancel(false)
            .setGroup("NGC")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Found an error")
                    .setSummaryText(message)
            )
            .setCategory(NotificationCompat.CATEGORY_SERVICE) // Heads-up notification
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (SDK_INT >= TIRAMISU) {
                ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 0)
            }
            return
        }
        notificationManager.notify(1001, notification)  // Unique ID for your notification
    }








    private fun showDialogs(inputText: String) {
        Toast.makeText(this, inputText, Toast.LENGTH_SHORT).show()
    }



    override fun onPause() {
        super.onPause()
        releaseWakeLock()
    }


    override fun onDestroy() {
        super.onDestroy()

        intentListener?.let {
            unregisterIntentListener(it)
        }
        intentListeners.clear()
        releaseWakeLock()
    }

}

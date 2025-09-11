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
import android.os.Build.VERSION
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.das.mediaHub.data.YouTuber.youtubeExtractor
import com.das.mediaHub.data.YouTuber.isValidYoutubeURL
import com.das.mediaHub.data.constants.Playback.PLAY_HERE_VIDEO
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.das.mediaHub.data.YouTuber.getAudioStreamUrl
import com.das.mediaHub.data.YouTuber.getVideoStreamUrl
import com.das.mediaHub.downloader.DownloaderClass
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.constants.Intents.NEW_INTENT_FOR_SEARCHER
import com.das.mediaHub.data.constants.Intents.NEW_INTENT_FOR_VIEWER
import com.das.mediaHub.data.constants.Intents.NEW_TEXT_FOR_RESULT
import com.das.mediaHub.data.YouTuber.extractPlaylistId
import com.das.mediaHub.data.YouTuber.getPlayListStreamUrl
import com.das.mediaHub.data.YouTuber.isValidYouTubePlaylistUrl
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
import com.das.mediaHub.data.constants.GlobalVideoList.bundles
import com.das.mediaHub.ui.players.videoPlayer.OnlineVideoPlayer
import com.das.mediaHub.ui.watchedVideos.WatchedVideosComposable
import com.das.mediaHub.NavScreens.*
import com.das.mediaHub.OnLaunchComponents.BottomNavItems
import com.das.mediaHub.PIP.shouldEnterPipMode
import com.das.mediaHub.data.constants.DownloadConstants.DOWNLOAD_FINISHED
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
    private var auth = Firebase.auth



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
    fun MainLauncherPageComposable() {

        val navController = rememberNavController()

        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        LaunchedEffect(Unit) {
            intent?.let {
                listenNewIntent(navController, it)
            }
        }

        DisposableEffect(Unit) {
            val listener: (Intent) -> Unit = {
                listenNewIntent(navController, it)
            }
            registerIntentListener(listener)
            onDispose {
                unregisterIntentListener(listener)
            }
        }



        val startDestination = remember { Home.route }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize(),
            bottomBar = {
                BottomNavItems(currentRoute, navController)
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
                    TopPopupNotification(
                        it,
                        onDismiss = {
                            showNotificationDialog = null
                        }
                    )
                }
            }

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                composable(Home.route) {
                    HomePageComposable(navController)
                }
                composable(RecentlyWatched.route) {
                    WatchedVideosComposable(navController)
                }
                composable(Setting.route) {
                    SettingsComposable(navController) {
                        showNotificationDialog = it
                    }
                }

                composable(VideoViewer.route) {
                    val bundle = bundles.getBundle(NEW_INTENT_FOR_VIEWER)
                    OnlineVideoPlayer(
                        navController = navController,
                        data = bundle
                    )
                }
                composable(ResultViewerPage.route) {

                    val argument = bundles.getString(NEW_TEXT_FOR_RESULT).toString()
                    ResultViewerPage(
                        navController,
                        argument
                    )
                }
                composable(Downloads.route) {
                    DownloadsComposable(navController)
                }
                composable(Searcher.route) {

                    SearchPageCompose(
                        navController,
                        bundles.getString(NEW_INTENT_FOR_SEARCHER, "")
                    )
                }
                composable(UserSettings.route) {
                    UserSettingComposable(navController)

                }
                composable(ExoPlayerUI.route) {
                    LocalVideoPlayer(
                        bundles.getString(PLAY_HERE_VIDEO).toString()
                    )
                }

                composable(Saved.route) {
                    WatchLaterComposable(navController)
                }

                composable(SignInPage.route) {
                    LoginPage(navController, auth)
                }
                composable(AccountSetting.route) {
                    if (auth.currentUser != null) {
                        AccountSettingsPage(navController, auth)
                    } else {
                        LoginPage(navController, auth)
                    }
                }
                composable(ChangePassword.route) {
                    ChangePasswordPage(navController, auth)
                }

                composable(WelcomePage.route) {
                    WelcomePage(navController) {
                        if (auth.currentUser == null) {
                            auth.signInAnonymously()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("Auth", "Signed in anonymously as ${auth.currentUser?.uid}")
                                    } else {
                                        Log.e("Auth", "Anonymous sign-in failed", task.exception)
                                    }
                                }
                        }
                        navController.run {
                            popBackStack()
                            navigate(Home.route)
                        }
                    }
                }
                composable(route = SignUpPage.route) {
                    SignUpPage(navController)
                }
                composable(route = FeedbackScreen.route) {
                    FeedbackComposable()
                }
            }
        }

    }





    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (shouldEnterPipMode) {

            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                params.apply {
                    setAspectRatio(Rational(16, 9))
                    if (VERSION.SDK_INT >= VERSION_CODES.S) {
                        setSeamlessResizeEnabled(true)
                    }
                }
                enterPictureInPictureMode(params.build())
            }
        }
    }


    private fun listenNewIntent(
        navController: NavController,
        newIntent: Intent
    ) {
        if (newIntent.action == Intent.ACTION_SEND) {
            val intentType = newIntent.type.toString()

            if (intentType.startsWith("text/")) {
                newTextIntent(
                    navController = navController,
                    sharedText = newIntent.getStringExtra(EXTRA_TEXT).toString()
                )
            } else if (intentType.startsWith("video/")) {
                newReceivedMediaTypeVideo(navController, newIntent)
            } else if (intentType.startsWith("audio/")) {
                newReceivedMediaTypeAudio(newIntent)
            }
        } else if (newIntent.action == Intent.ACTION_VIEW) {
            newMediaIntent(navController, newIntent.data)
        } else if (newIntent.action == DOWNLOAD_FINISHED) {
            val apkPath = newIntent.getStringExtra("apk_path") ?: return
            val apkFile = File(apkPath)
            requestToInstall(apkFile)
        } else if (newIntent.action == Intent.ACTION_APPLICATION_PREFERENCES) {
            navController.navigate(Setting.route)
        }
    }


    private fun requestToInstall(apkFile: File) {

        val apkUri = FileProvider.getUriForFile(
            this,
            "${this.packageName}.file-provider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivity(installIntent)
    }

    override fun onStart() {
        super.onStart()
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationChannels(this).createAllNotificationChannels()
        }


        if (VERSION.SDK_INT >= TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    arrayOf(
                        POST_NOTIFICATIONS,
                        READ_MEDIA_VIDEO,
                        READ_MEDIA_AUDIO
                    ).toString())
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(
                        POST_NOTIFICATIONS,
                        READ_MEDIA_VIDEO,
                        READ_MEDIA_AUDIO
                    ), 1)
            }
        }
    }







    private fun newReceivedMediaTypeVideo(navController: NavController, myIntent: Intent){

        @Suppress("DEPRECATION")
        val videoUri = if (VERSION.SDK_INT >= TIRAMISU) {
            myIntent.getParcelableExtra(
                EXTRA_STREAM, Uri::class.java)
        } else myIntent.getParcelableExtra(EXTRA_STREAM)

        bundles.putString(PLAY_HERE_VIDEO, videoUri.toString())
        navController.navigate(ExoPlayerUI.route)

    }

    private fun newReceivedMediaTypeAudio(
        myIntent: Intent
    ){
        @Suppress("DEPRECATION")
        val audioUri = if (VERSION.SDK_INT >= TIRAMISU) myIntent.getParcelableExtra(EXTRA_STREAM, Uri::class.java)
        else myIntent.getParcelableExtra(EXTRA_STREAM)

        val playIntent = Intent(this, BackGroundPlayer::class.java).apply {
            action = ACTION_START
            putExtra("media_id", audioUri?.path)
            putExtra("media_url", audioUri?.path)
            putExtra("title", title)
        }
        startService(playIntent)
    }

    private fun newMediaIntent(
        navController: NavController,
        mediaUri: Uri?
    ){
        mediaUri?.let {
            val mimeType = contentResolver.getType(it) ?: ""
            if (mimeType.startsWith("video/")) {
                bundles.putString(PLAY_HERE_VIDEO, intent.dataString)
                navController.navigate(ExoPlayerUI.route)
            } else if (mimeType.startsWith("audio/")) {


                val playIntent = Intent(this, BackGroundPlayer::class.java).apply {
                    action = ACTION_START
                    putExtra("media_id", it.path)
                    putExtra("media_url", it.path)
                    putExtra("title", title)
                }
                startService(playIntent)

            } else {
                showDialogs("Unsupported media type")
            }
        }
    }
    private fun newTextIntent(
        navController: NavController,
        sharedText: String
    ) {
        sharedText.let {
            if (isValidYoutubeURL(it)) {
                val videoId = youtubeExtractor(it)
                val bundle= Bundle().apply {
                    putString("View_ID", videoId)
                    putString("View_URL", "https://www.youtube.com/watch?v=$videoId")
                }
                bundles.apply {
                    putBundle(NEW_INTENT_FOR_VIEWER, bundle)
                }
                navController.navigate(VideoViewer.route)

            } else if (isValidYouTubePlaylistUrl(it)){
                val bundle= Bundle().apply {
                    putString("View_ID", extractPlaylistId(it))
                    putString("View_URL", it)
                }
                bundles.apply {
                    putBundle(NEW_INTENT_FOR_VIEWER, bundle)
                }
                navController.navigate(VideoViewer.route)

            }else if (it.startsWith("DownloadsPageFr")) {
                navController.navigate(Downloads.route)
            } else {
                bundles.apply {
                    putString(NEW_INTENT_FOR_SEARCHER, it)
                }
                navController.navigate(Searcher.route)
            }
        }
    }

    fun startDownloadingVideo(videoId: String, title: String){
        val downloaderClass = DownloaderClass(this)

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
                this,
                POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (VERSION.SDK_INT >= TIRAMISU) {
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
        WakeLockHelper.releaseWakeLock(this)
    }


    override fun onDestroy() {
        super.onDestroy()

        intentListener?.let {
            unregisterIntentListener(it)
        }
        intentListeners.clear()
        WakeLockHelper.releaseWakeLock(this)
    }

}

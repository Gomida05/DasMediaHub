package com.das.forui

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TEXT
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.das.forui.objectsAndData.Youtuber.youtubeExtractor
import com.das.forui.objectsAndData.Youtuber.isValidYoutubeURL
import com.das.forui.objectsAndData.ForUIKeyWords.PLAY_HERE_VIDEO
import com.das.forui.objectsAndData.ForUIDataClass.MyBottomNavData
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.das.forui.objectsAndData.Youtuber.getAudioStreamUrl
import com.das.forui.objectsAndData.Youtuber.getVideoStreamUrl
import com.das.forui.downloader.DownloaderClass
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_START
import com.das.forui.objectsAndData.ForUIKeyWords.NEW_INTENT_FOR_SEARCHER
import com.das.forui.objectsAndData.ForUIKeyWords.NEW_INTENT_FOR_VIEWER
import com.das.forui.objectsAndData.ForUIKeyWords.NEW_TEXT_FOR_RESULT
import com.das.forui.objectsAndData.Youtuber.extractPlaylistId
import com.das.forui.objectsAndData.Youtuber.getPlayListStreamUrl
import com.das.forui.objectsAndData.Youtuber.isValidYouTubePlaylistUrl
import com.das.forui.services.BackGroundPlayer
import com.das.forui.ui.home.downloads.DownloadsPageComposable
import com.das.forui.ui.home.HomePageComposable
import com.das.forui.ui.home.searcher.result.ResultViewerPage
import com.das.forui.ui.home.searcher.SearchPageCompose
import com.das.forui.ui.settings.watch_later.WatchLaterComposable
import com.das.forui.ui.settings.SettingsComposable
import com.das.forui.ui.settings.userSettings.UserSettingComposable
import com.das.forui.ui.home.downloads.videoPlayerLocally.ExoPlayerUI
import com.das.forui.ui.welcome.LoginPage
import com.das.forui.ui.welcome.SignUpPage
import com.das.forui.ui.viewer.GlobalVideoList.bundles
import com.das.forui.ui.viewer.VideoPlayerScreen
import com.das.forui.ui.watchedVideos.WatchedVideosComposable
import com.das.forui.Screen.*
import com.das.forui.theme.CustomTheme
import com.das.forui.ui.settings.FeedbackComposable
import com.das.forui.ui.welcome.WelcomePage


class MainActivity : ComponentActivity() {

    private val intentListeners = mutableSetOf<(Intent) -> Unit>()

    private var intentListener: ((Intent) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

        val bottomNavigationItems = listOf(
            MyBottomNavData(
                title = Home.route,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home
            ),
            MyBottomNavData(
                title = RecentlyWatched.route,
                selectedIcon = Icons.Filled.WatchLater,
                unselectedIcon = Icons.Outlined.WatchLater
            ),
            MyBottomNavData(
                title = Setting.route,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings
            )
        )

        Scaffold(
            bottomBar = {
                if (currentRoute in listOf(Home.route, RecentlyWatched.route, Setting.route)) {

                    NavigationBar(
                        windowInsets = NavigationBarDefaults.windowInsets,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12))
                    ) {
                        bottomNavigationItems.forEachIndexed { _, items ->
                            NavigationBarItem(
                                selected = currentRoute == items.title,
                                onClick = {
                                    if (currentRoute != items.title) {
                                        navController.navigate(items.title) {
                                            // Avoid multiple copies of the same destination
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (currentRoute == items.title) items.selectedIcon else items.unselectedIcon,
                                        contentDescription = items.title
                                    )
                                },
                                label = {
                                    Text(text = items.title)
                                }
                            )
                        }

                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier
                .fillMaxSize()
        ) { paddingValues ->

//                val isUserLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
            val startDestination =
                Home.route //if (isUserLoggedIn) Home.route else WelcomePage.route
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
                    SettingsComposable(navController)
                }

                composable(VideoViewer.route) {
                    val bundle = bundles.getBundle(NEW_INTENT_FOR_VIEWER)
                    VideoPlayerScreen(
                        navController,
                        bundle
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
                    DownloadsPageComposable(navController)
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
                    ExoPlayerUI(
                        bundles.getString(PLAY_HERE_VIDEO).toString()
                    )
                }

                composable(Saved.route) {
                    WatchLaterComposable(navController)
                }

                composable(LoginPage1.route) {
                    LoginPage(navController)
                }
                composable(WelcomePage.route) {
                    WelcomePage(navController)
                }
                composable(SignUpPage.route) {
                    SignUpPage(navController)
                }
                composable(FeedbackScreen.route) {
                    FeedbackComposable()
                }
            }
        }
    }

    private fun listenNewIntent(
        navController: NavController,
        newIntent: Intent
    ){
        if (newIntent.action == Intent.ACTION_SEND) {
            val intentType = newIntent.type.toString()

            if (intentType.startsWith("text/")){
                newTextIntent(
                    navController,
                    newIntent.getStringExtra(EXTRA_TEXT).toString()
                )
            }
            else if (intentType.startsWith("video/"))
            {
                newReceivedMediaTypeVideo(navController, newIntent)
            }
            else if (intentType.startsWith("audio/"))
            {
                newReceivedMediaTypeAudio(newIntent)
            }
        } else if (newIntent.action == Intent.ACTION_VIEW) {
            newMediaIntent(navController, newIntent.data)
        }
    }

    override fun onStart() {
        super.onStart()
        createNotificationChannel()
        createGroupNotificationChannel()
        createMediaGroupNotificationChannel()
        if (Build.VERSION.SDK_INT >= TIRAMISU) {
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
        val videoUri = if (Build.VERSION.SDK_INT >= TIRAMISU) {
            myIntent.getParcelableExtra(
                EXTRA_STREAM, Uri::class.java)
        } else myIntent.getParcelableExtra(EXTRA_STREAM)

        bundles.putString(PLAY_HERE_VIDEO, videoUri.toString())
        navController.navigate("ExoPlayerUI")

    }

    private fun newReceivedMediaTypeAudio(
        myIntent: Intent
    ){
        @Suppress("DEPRECATION")
        val audioUri = if (Build.VERSION.SDK_INT >= TIRAMISU) {
            myIntent.getParcelableExtra(
                EXTRA_STREAM, Uri::class.java)
        } else myIntent.getParcelableExtra(EXTRA_STREAM)
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

    fun startDownloadingVideo(context: Context, videoId: String, title: String){

        getVideoStreamUrl(videoId,
            onSuccess = {
                DownloaderClass(context)
                    .downloadVideo(it, title)
            },
            onFailure = {
                showDialogs(it)
            }
        )
    }

    fun startPlayListDownload(
        context: Context, playListUrl: String
    ){
        getPlayListStreamUrl(
            playListUrl,
            onSuccess = { playListName, videoList ->
                for (i in videoList){
                    DownloaderClass(context)
                        .downloadVideosPlayList(
                            i.url,
                            playListName,
                            i.title
                        )
                }
            },
            onFailure = {
                Log.e("There is an error ", it)
            }
        )
    }


    fun startDownloadingAudio(context: Context, videoId: String, title: String){

        getAudioStreamUrl(videoId,
            onSuccess = {
                DownloaderClass(context)
                    .downloadMusic(it, title)
            },
            onFailure = {
                showDialogs(it)
            }
        )
    }


    private fun createMediaGroupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannelGroup(
                "MNGC",
                "MediaPlayer notifications"
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannelGroup(serviceChannel)
        }
    }
    
    private fun createGroupNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannelGroup(
                "NGC",
                "Download notification"
            )


            val downloadChannelId = "download_channel"
            val downloadChannelName = "Downloads"
            val downloadChannel = NotificationChannel(
                downloadChannelId,
                downloadChannelName,
                NotificationManager.IMPORTANCE_LOW // Set the importance level based on your needs
            )
            downloadChannel.apply {
                group = "NGC"
                description = "This channel is for download notifications"
            }


            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannelGroup(serviceChannel)
            manager?.createNotificationChannel(downloadChannel)
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
                    .setSummaryText("Please contact the developer")
            )
            .setCategory(NotificationCompat.CATEGORY_SERVICE) // Heads-up notification
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= TIRAMISU) {
                ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 0)
            }
            return
        }
        notificationManager.notify(1001, notification)  // Unique ID for your notification
    }

    private fun createNotificationChannel() {
        // Only create the channel for Android 8.0 (API level 26) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "error_searching"
            val channelName = "Error Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for error notifications"
                enableVibration(true)
            }

            val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }





    private fun showDialogs(inputText: String) {
        Toast.makeText(this, inputText, Toast.LENGTH_SHORT).show()
    }



    fun requestAudioFocusFromMain(context: Context, exoPlayer: ExoPlayer?) {

        val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

        val audioFocusRequest = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    // Pause playback when losing focus
                    exoPlayer?.playWhenReady = false
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // Resume playback when gaining focus
                    exoPlayer?.playWhenReady = true
                    exoPlayer?.volume = 1.0f
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // Pause temporarily (e.g., during a phone call)
                    exoPlayer?.playWhenReady = false
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    // Can continue playing but with lower volume
                    exoPlayer?.volume = 0.1f  // Reduce volume
                }
            }
        }


        @Suppress("DEPRECATION")
        val result = audioManager.requestAudioFocus(
            audioFocusRequest,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        intentListener?.let { unregisterIntentListener(it) }
        intentListeners.clear()
    }

}

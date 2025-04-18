package com.das.forui

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.UiModeManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TEXT
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
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
import com.das.forui.MainApplication.Youtuber.youtubeExtractor
import com.das.forui.MainApplication.Youtuber.isValidYoutubeURL
import com.das.forui.MainApplication.Youtuber.pythonInstant
import com.das.forui.objectsAndData.ForUIKeyWords.PLAY_HERE_VIDEO
import com.das.forui.objectsAndData.ForUIDataClass.MyBottomNavData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.System.currentTimeMillis
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.das.forui.databased.PathSaver.getAudioDownloadPath
import com.das.forui.databased.PathSaver.getVideosDownloadPath
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_START
import com.das.forui.objectsAndData.ForUIKeyWords.NEW_INTENT_FOR_SEARCHER
import com.das.forui.objectsAndData.ForUIKeyWords.NEW_INTENT_FOR_VIEWER
import com.das.forui.objectsAndData.ForUIKeyWords.NEW_TEXT_FOR_RESULT
import com.das.forui.services.BackGroundPlayer
import com.das.forui.ui.downloads.DownloadsPageComposable
import com.das.forui.ui.home.HomePageComposable
import com.das.forui.ui.result.ResultViewerPage
import com.das.forui.ui.searcher.SearchPageCompose
import com.das.forui.ui.watch_later.WatchLaterComposable
import com.das.forui.ui.settings.SettingsComposable
import com.das.forui.ui.userSettings.UserSettingComposable
import com.das.forui.ui.videoPlayerLocally.ExoPlayerUI
import com.das.forui.ui.viewer.GlobalVideoList.bundles
import com.das.forui.ui.viewer.VideoPlayerScreen


class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            MainLauncherPageComposable()
        }

    }




    @Composable
    fun MainLauncherPageComposable() {

        val navController = rememberNavController()


        val mContext = LocalContext.current

        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route


        val activity = (mContext.getActivity() as ComponentActivity)
        val listener = Consumer<Intent> {
            listenNewIntent(navController, it)
        }


        val lifecycleOwner = LocalLifecycleOwner.current

        LaunchedEffect(lifecycleOwner) {
            activity.addOnNewIntentListener(listener)
        }


        CustomTheme {
            val bottomNavigationItems = listOf(
                MyBottomNavData(
                    title = "Home",
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home
                ),
                MyBottomNavData(
                    title = "Watch Later",
                    selectedIcon = Icons.Filled.WatchLater,
                    unselectedIcon = Icons.Outlined.WatchLater
                ),
                MyBottomNavData(
                    title = "Setting",
                    selectedIcon = Icons.Filled.Settings,
                    unselectedIcon = Icons.Outlined.Settings
                )
            )

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (currentRoute in listOf("Home", "Watch Later", "Setting")) {

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
                }
            ) { paddingValues ->

                NavHost(
                    navController = navController, startDestination = "Home",
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable("Home") {
                        HomePageComposable(navController)
                    }
                    composable("Watch Later") {
                        WatchLaterComposable(navController)
                    }
                    composable("Setting") {
                        SettingsComposable(navController)
                    }

                    composable("video viewer") {
                        val bundle = bundles.getBundle(NEW_INTENT_FOR_VIEWER)
                        VideoPlayerScreen(
                            navController,
                            bundle
                        )

                    }
                    composable("ResultViewerPage") {

                        val argument = bundles.getString(NEW_TEXT_FOR_RESULT).toString()
                        ResultViewerPage(
                            navController,
                            argument
                        )
                    }
                    composable("Downloads") {
                        DownloadsPageComposable(navController)
                    }
                    composable("searcher") {

                        SearchPageCompose(
                            navController,
                            bundles.getString(NEW_INTENT_FOR_SEARCHER, "")
                        )
                    }
                    composable("user Setting") {
                        UserSettingComposable(navController)

                    }
                    composable("ExoPlayerUI") {
                        ExoPlayerUI(
                            navController,
                            bundles.getString(PLAY_HERE_VIDEO).toString()
                        )
                    }
                }
            }

        }



        DisposableEffect(Unit) {

            onDispose {
                activity.removeOnNewIntentListener(listener)
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
                navController.navigate("ExoPlayerUI")
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
                navController.navigate("video viewer")

            } else if (it.startsWith("DownloadsPageFr")) {
                navController.navigate("Downloads")
            } else {
                bundles.apply {
                    putString(NEW_INTENT_FOR_SEARCHER, it)
                }
                navController.navigate("searcher")
            }
        }
    }




    fun downloadVideo(link: String, title: String, contexts: Context) {
        val path = getVideosDownloadPath(contexts)

        createSingleDirectory(path)
        try {
            var forToast: String
            val mainFile = pythonInstant.getModule("main")
            val variable = mainFile["DownloadVideo"]
            CoroutineScope(Dispatchers.IO).launch {
                when (val tester = variable?.call(link, path).toString()) {
                    "False" -> {
                        forToast = tester
                    }
                    "None" -> {
                        forToast = "couldn't find it!"
                    }
                    else -> {
                        tester.let {
                            val file = File(it)
                            if (file.exists()) {
                                MediaScannerConnection.scanFile(
                                    contexts,
                                    arrayOf(file.toString()),
                                    null,
                                    null
                                )
                                forToast = "$title has been downloaded successfully go check it out!"

                            } else {
                                Log.e("MainActivity", "File does not exist at path: $it")
                                forToast = "Download interrupted by the internet please try again!"

                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    showDialogs(forToast)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error: ${e.message}")
            showDialogs("An Exception Error from kt: \n$e")
        }
    }






    fun downloadMusic(receivedLink: String, title: String, context: Context) {
        val path = getAudioDownloadPath(context)
        println("given path \n$path")
        createSingleDirectory(path)
        try {
            var forToast: String
            val mainFile = pythonInstant.getModule("main")
            val variable = mainFile["DownloadMusic"]
            CoroutineScope(Dispatchers.IO).launch {
                when (val tester = variable?.call(receivedLink, path).toString()) {
                    "False" -> {
                        forToast = "Something went wrong"
                    }

                    "None" -> {
                        forToast = "couldn't find it!"
                    }

                    "we trying" -> {
                        downloadMusic(receivedLink, title, context)
                    }

                    else -> {
                        tester.let {
                            val file = File(it)
                            if (file.exists()) {
                                MediaScannerConnection.scanFile(
                                    context,
                                    arrayOf(file.toString()),
                                    null,
                                    null
                                )
                                downloadCompleted("$title has been downloaded successfully go check it out!")
                                forToast =
                                    "$title has been downloaded successfully go check it out!"
                            } else {
                                Log.e("MainActivity", "File does not exist at path: $it")
                                forToast = "Download interrupted by the internet please try again!"
                            }
                        }
                        withContext(Dispatchers.Main) {
                            showDialogs(forToast)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error: ${e.message}")
            showDialogs("An Exception Error from kt: \n$e")
        }
    }


    private fun downloadCompleted(message: String) {


        val notificationId = currentTimeMillis().toInt()
        val notification = NotificationCompat.Builder(this, "download_channel")
            .setContentTitle("Download Finished")
            .setContentText(message)
            .setSmallIcon(R.mipmap.icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroupSummary(false)
            .setGroup("message")
            .build()
        val notificationManager = NotificationManagerCompat.from(this@MainActivity)
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
        notificationManager.notify(notificationId, notification)
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
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = "Channel for error notifications"

            val notificationManager =
                applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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







    private fun createSingleDirectory(directoryPath: String) {
        val dir = File(directoryPath)
        if (dir.mkdir()) {
        } else {
        }
    }




    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val myNewConfig = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val currentUiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val sharedPref: SharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        val uiModeType = sharedPref.getInt("isNightModeOn", currentUiMode)

        if (myNewConfig != currentUiMode && uiModeType == UiModeManager.MODE_NIGHT_AUTO) {
            if (myNewConfig == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            this.recreate()
        }
    }


    private fun Context.getActivity(): Activity {
        if (this is Activity) return this
        return if (this is ContextWrapper) baseContext.getActivity() else getActivity()
    }

}

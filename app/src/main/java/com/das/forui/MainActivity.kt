@file:Suppress("DEPRECATION")
package com.das.forui

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TEXT
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.chaquo.python.Python.getInstance
import com.das.forui.MainActivity.Youtuber.PLAY_HERE_AUDIO
import com.das.forui.MainActivity.Youtuber.PLAY_HERE_VIDEO
import com.das.forui.MainActivity.Youtuber.pythonInstant
import com.das.forui.databased.PathSaver
import com.das.forui.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.System.currentTimeMillis
import java.net.URL
import java.util.regex.Pattern



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()


        onNewIntent(intent)


        setupActionBarWithNavController(
            findNavController(R.id.nav_host_fragment_activity_main),
            AppBarConfiguration(
                setOf(
                    R.id.navigation_home,
                    R.id.nav_watch_later,
                    R.id.navigation_settings
                )
            )
        )
        binding.navView.setupWithNavController(
            findNavController(R.id.nav_host_fragment_activity_main)
        )



    }



    override fun onStart() {
        super.onStart()
        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 1)
            }
        }
    }



    fun startBanner(startOrStop: Boolean){
        try {
            MobileAds.initialize(this@MainActivity) { }
            val mAdView: AdView = findViewById(R.id.adView)
            if (startOrStop) {
                val startTime = System.nanoTime()
                val elapsedTime = System.nanoTime() - startTime
                Log.d("AppStartup", "Mobile Ads initialization took: ${elapsedTime / 1_000_000} ms")
                val adRequest = AdRequest.Builder().build()
                mAdView.loadAd(adRequest)
            } else {
                mAdView.destroy()
                }
        } catch (e: Exception) {
                Log.e("MainActivity", "Error destroying AdView: ${e.message}")
        }

    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.action == Intent.ACTION_SEND) {
            val intentType = intent.type.toString()
            if (intentType.startsWith("text/")){
                newTextIntent(intent.getStringExtra(EXTRA_TEXT).toString())
            }
            else if (intentType.startsWith("video/"))
            {
                newReceivedMediaTypeVideo(intent)
            }
            else if (intentType.startsWith("audio/"))
            {
                newReceivedMediaTypeAudio(intent)
            }
        } else if (intent.action == Intent.ACTION_VIEW) {
            newMediaIntent(intent.data)
        }

    }


    private fun newReceivedMediaTypeVideo(myIntent: Intent){

        @Suppress("DEPRECATION")
        val videoUri: Uri? = myIntent.getParcelableExtra(EXTRA_STREAM)
        val bundle = Bundle().apply {
            putString(PLAY_HERE_VIDEO, videoUri.toString())
        }
        findNavController(R.id.nav_host_fragment_activity_main).navigate(
            R.id.nav_fullscreen,
            bundle
        )
    }

    private fun newReceivedMediaTypeAudio(myIntent: Intent){
        @Suppress("DEPRECATION")
        val audioUri: Uri? = myIntent.getParcelableExtra(EXTRA_STREAM)
        val bundle = Bundle().apply {
            putString(PLAY_HERE_AUDIO, audioUri.toString())
        }
        findNavController(R.id.nav_host_fragment_activity_main).navigate(
            R.id.nav_fullscreen,
            bundle
        )
    }

    private fun newMediaIntent(mediaUri: Uri?){
        mediaUri?.let {
            val mimeType = contentResolver.getType(it) ?: ""
            if (mimeType.startsWith("video/")) {
                val bundle = Bundle().apply {
                    putString(PLAY_HERE_VIDEO, intent.dataString)
                }
                findNavController(R.id.nav_host_fragment_activity_main).navigate(
                    R.id.nav_fullscreen,
                    bundle
                )
            } else if (mimeType.startsWith("audio/")) {
                val bundle = Bundle().apply {
                    putString(PLAY_HERE_AUDIO, intent.dataString)
                }
                findNavController(R.id.nav_host_fragment_activity_main).navigate(
                    R.id.nav_fullscreen,
                    bundle
                )
            } else {
                showDialogs("Unsupported media type")
            }
        }
    }
    private fun newTextIntent(sharedText: String) {
        sharedText.let {
            if (isValidYoutubeURL(it)) {
                val videoId = Youtuber.extractor(it)
                val bundle = Bundle().apply {
                    putString("View_ID", videoId)
                    putString("View_URL", "https://www.youtube.com/watch?v=$videoId")
                }

                findNavController(R.id.nav_host_fragment_activity_main).navigate(
                    R.id.nav_video_viewer,
                    bundle
                )
            } else if (it.startsWith("DownloadsPageFr")) {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_Downloads)
            } else {
                val bundle = Bundle().apply {
                    putString(EXTRA_TEXT, it)
                }
                findNavController(R.id.nav_host_fragment_activity_main).navigate(
                    R.id.nav_searcher,
                    bundle
                )
            }
        }
    }









    fun callPythonSearchWithLink(inputText: String): Map<String, Any>? {
        return try {
            val mainFile = pythonInstant.getModule("main")
            val variable = mainFile["SearchWithLink"]
            val result = variable?.call("https://www.youtube.com/watch?v=$inputText")
            println("python: $result")
            val jsonString = result.toString()
            // Use Gson to parse the JSON string into a Map
            val resultMapType = object : TypeToken<Map<String, Any>>() {}.type
            val resultMap: Map<String, Any> = Gson().fromJson(jsonString, resultMapType)
            resultMap

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    fun downloadVideo(link: String, title: String, contexts: Context) {
        val path = PathSaver().getVideosDownloadPath(contexts)
//                createSingleDirectory("/storage/emulated/0/Movies/ForUI")
        createSingleDirectory(path.toString())
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
        val path = PathSaver().getMusicDownloadPath(context)
        println("given path \n$path")
        createSingleDirectory(path.toString())
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


    fun downloadCompleted(message: String) {


        val notificationId = currentTimeMillis().toInt()
        val notification = NotificationCompat.Builder(this, "error_searching")
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
            ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 0)
            return
        }
        notificationManager.notify(notificationId, notification)
    }


    fun alertUserError(message: String?) {
        // Use applicationContext to ensure global access
        val context = application.applicationContext


        val notification = NotificationCompat.Builder(context, "error_searching")
            .setContentTitle("Found an error")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher_ofme)
            .setOngoing(true)
            .addAction(R.drawable.pause_icon,"Pause", null)
            .addAction(R.drawable.play_arrow_24dp,"Play", null)
            .addAction(R.drawable.stop_circle_24dp, "Stop", null)
            .setAutoCancel(false)
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
            ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 0)
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




    fun showDialogs(inputText: String) {
        Toast.makeText(this, inputText, Toast.LENGTH_SHORT).show()
    }



    fun isValidYoutubeURL(youTubeUrl: String): Boolean {
        try {
            val trimmedUrl = youTubeUrl.trim()
            val cleanedUrl = if (trimmedUrl.endsWith("&feature=shared")) trimmedUrl.removeSuffix("&feature=shared") else trimmedUrl

            val url = URL(cleanedUrl)

            val host = url.host
            if (host == "www.youtube.com" || host == "youtube.com") {
                val videoPattern = Pattern.compile("^/watch\\?v=([A-Za-z0-9_-]{11})$")
                val matcher = videoPattern.matcher(url.path + "?" + url.query)  // Combine path and query
                return matcher.matches()
            } else if (host == "youtu.be") {
                // Shortened YouTube URL (youtu.be/VIDEO_ID)
                val videoPattern = Pattern.compile("^/([A-Za-z0-9_-]{11})$")
                val matcher = videoPattern.matcher(url.path)  // Check the path only
                return matcher.matches()
            }

            // If not youtube.com or youtu.be, return false
            return false
        } catch (e: Exception) {
            println("yes with me3 ${e.message}")
//            alertUserError(e.message)
            return false
        }
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


        val result = audioManager.requestAudioFocus(
            audioFocusRequest,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

        }
        when (exoPlayer?.isPlaying){
            true ->{

            }
            false ->{

            }

            else -> {

            }
        }
    }





    fun createSingleDirectory(directoryPath: String) {
        val dir = File(directoryPath)
        if (dir.mkdir()) {
        } else {
        }
    }

    fun hideBottomNav() {
        findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE
    }

    fun showBottomNav() {
        findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE
    }


    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
            val currentFragment = navHostFragment.childFragmentManager.fragments.lastOrNull().toString()
            if (currentFragment.startsWith("ViewerFragment")) {



                val aspectRatio = Rational(14, 9)
                val pipBuilder = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build()
                enterPictureInPictureMode(pipBuilder)
            }
        }
    }



    object Youtuber{
        val pythonInstant = getInstance()
        const val PLAY_HERE_VIDEO = "com.das.forui.PLAY_HERE_VIDEO"
        const val PLAY_HERE_AUDIO = "com.das.forui.PLAY_HERE_AUDIO"
        fun extractor(url: String): String? {
            val regex= "(?<=v=|/)([a-zA-Z0-9_-]{11})(?=&|\$|/)"
            val pattern= Regex(regex)
            val match=pattern.find((url))
            return match?.groups?.get(1)?.value
        }
    }
}

package com.das.forui


import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.util.Rational
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.das.forui.databased.DatabaseHelper1
import com.das.forui.databased.PathSaver
import com.das.forui.databinding.ActivityMainBinding
import com.das.forui.services.AudioServiceFromUrl
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
import java.net.URL
import java.util.regex.Pattern



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        if (!Python.isStarted()) {
            val startTime= System.nanoTime()
            Python.start(AndroidPlatform(this))
            val elapsedTime = System.nanoTime() - startTime
            Log.d("AppStartup", "Python initialization took: ${elapsedTime / 1_000_000} ms")
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
            } else {
                val uri = Uri.parse("package:$packageName")
                val intenser = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                startActivity(intenser)
            }
        }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 1)
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)

        /*
        TODO WE WILL ADD THE FUNCTIONALITY HERE SO IT GONNA BE EASY TO RECREATE THE ACTIVITY
         */
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
        if (intent.action == Intent.ACTION_SEND && intent.type.toString().startsWith("text/")) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT).toString()
            println("here is it my man $sharedText")
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
                }
                else if (it.startsWith("DownloadsPageFr")){
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_Downloads)
                }
                else {
                    val bundle = Bundle().apply {
                        putString("EXTRA_ONE", it)
                    }
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(
                        R.id.nav_searcher,
                        bundle
                    )
                }
            }
        }
    }











    fun callPythonSearchWithLink(inputText: String): Map<String, Any>? {
        return try {
            val py = Python.getInstance()
            val mainFile = py.getModule("main")
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
            val py = Python.getInstance()
            val mainFile = py.getModule("main")
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
                                DatabaseHelper1(contexts).insertData(title, path = tester)
                            } else {
                                Log.e("MainActivity", "File does not exist at path: $it")
                                forToast = "Download interrupted by the internet please try again!"

                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    showDiaglo(forToast)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error: ${e.message}")
            showDiaglo("An Exception Error from kt: \n$e")
        }
    }







    fun downloadMusic(receivedLink: String, title: String, context: Context) {
        val path = PathSaver().getMusicDownloadPath(context)
        println("given path \n$path")
        createSingleDirectory(path.toString())
        try {
            var forToast: String
            val py = Python.getInstance()
            val mainFile = py.getModule("main")
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
                                println("filler is here \n $path")
                                DatabaseHelper1(context).insertData(title, path = file.toString())
//                            withContext(Dispatchers.Main) {
                                justAlertUser("$title has been downloaded successfully go check it out!")
                                Log.e("MainActivity", "here is it \n${file}")
                                Log.d("MainActivity", "File scan initiated for ")
                                forToast =
                                    "$title has been downloaded successfully go check it out!"
                            } else {
                                Log.e("MainActivity", "File does not exist at path: $it")
                                forToast = "Download interrupted by the internet please try again!"
                            }
                        }
                        withContext(Dispatchers.Main) {
                            showDiaglo(forToast)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error: ${e.message}")
            showDiaglo("An Exception Error from kt: \n$e")
        }
    }

    @SuppressLint("MissingPermission")
    private fun justAlertUser(message: String?) {
        // Use applicationContext to ensure global access
        val context = application.applicationContext

        // Create the notification
        val notification = NotificationCompat.Builder(context, "error_searching")
            .setContentTitle("Download Finished")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher_ofme)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Download Finished")

            )
            .setCategory(NotificationCompat.CATEGORY_SERVICE) // Heads-up notification
            .build()

        // Get the NotificationManager system service and display the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1001, notification)  // Unique ID for your notification
    }

    @SuppressLint("MissingPermission")
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

        // Get the NotificationManager system service and display the notification
        val notificationManager = NotificationManagerCompat.from(context)
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



    fun createMediaNotification(title: String) {

        val thisIntent = Intent(this, this::class.java)

        val pendingIntent = PendingIntent.getActivity(this,0, thisIntent, PendingIntent.FLAG_IMMUTABLE)


        val mediaStyle = MediaStyle()
            .setShowActionsInCompactView(0, 1, 2)


        val ourNotification = NotificationCompat.Builder(this, "error_searching")
            .setContentTitle(title)
            .setContentText("From ForUI App")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.music_note_24dp)
            .setOngoing(true)
            .addAction(R.drawable.pause_icon, "Pause", null)
            .addAction(R.drawable.play_arrow_24dp, "Play", null)
            .addAction(R.drawable.stop_circle_24dp, "Stop", null)
            .setStyle(mediaStyle)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setProgress(100, 25, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setSound(null)
            .setVibrate(longArrayOf(0))
            .build()
        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            //here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(101, ourNotification)
    }


    fun showDiaglo(inputText: String) {
        Toast.makeText(this, inputText, Toast.LENGTH_SHORT).show()
    }



    fun isValidYoutubeURL(youTubeUrl: String): Boolean {
        try {
            val trimmedUrl = youTubeUrl.trim()
            val cleanedUrl = if (trimmedUrl.endsWith("&feature=shared")) {
                trimmedUrl.removeSuffix("&feature=shared")
            } else {
                trimmedUrl
            }
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
        fun extractor(url: String): String? {
            val regex= "(?<=v=|/)([a-zA-Z0-9_-]{11})(?=&|\$|/)"
            val pattern= Regex(regex)
            val match=pattern.find((url))
            return match?.groups?.get(1)?.value
        }
    }
    fun videoSetting(view: View) {}

    override fun onDestroy() {
        if (AudioServiceFromUrl().exoPlayerFromAudioService?.isPlaying == true){
            Toast.makeText(this, "Can't close it!", Toast.LENGTH_SHORT).show()

        }
        super.onDestroy()
    }
}

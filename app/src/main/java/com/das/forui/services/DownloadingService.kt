package com.das.forui.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.chaquo.python.Python
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.databased.PathSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DownloadingService(private val channelId: String) : Service() {


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        createNotificationChannel()
        super.onCreate()
    }




    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, intent, PendingIntent.FLAG_MUTABLE
        )
        val downloadType = intent?.getStringExtra("download_type") ?: ""
        val link = intent?.getStringExtra("link") ?: ""
        val title = intent?.getStringExtra("title") ?: ""

        // Check the download type and call the appropriate method
        if (downloadType == "music") {
            downloadMusic(link, title, this)
        } else if (downloadType == "video") {
            downloadVideo(link, title, this)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Service Running")
            .setContentText("Your service is running in the background")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setOngoing(true)
            .setAutoCancel(false)
            .setProgress(0,20, true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().setBigContentTitle("hello there").setSummaryText("this is for service that service is running in the background"))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)// This line makes it a heads-up notification
            .build()

        startForeground(2, notification)

        // Your service-related work here
//            val py = Python.getInstance()
//            val mainFile = py.getModule("main")
//            val variable = mainFile.callAttr("Service").toString()
//            Log.d("MainActivity", variable)

        Thread {
        }.start()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadingServiceChannel = NotificationChannel(
                channelId,
                "Downloading Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW).apply {
                setShowBadge(true)
                description = "channelDescription"
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(downloadingServiceChannel)
        }
    }


    private fun downloadMusic(received: String, title: String, context: Context) {
        val path = PathSaver().getAudioDownloadPath(context)
        MainActivity().createSingleDirectory(path.toString())
        try {
            val py = Python.getInstance()
            val mainFile = py.getModule("main")
            var forToast: String


            CoroutineScope(Dispatchers.IO).launch {
                val variable = mainFile["DownloadMusic"]
                val tester = variable?.call(received, path)?.toString()
                val result: String = tester ?: "No result from Python"
                forToast = result

                tester?.let {
                    val file = File(it)
                    if (file.exists()) {
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(file.toString()),
                            null,
                            null
                        )

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
                    showDiagloForDownloading(forToast)
//                    stopSelf()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error: ${e.message}")
            showDiagloForDownloading("An Exception Error from kt: \n$e")
        }
    }


    fun downloadVideo(link: String, title: String, contexts: Context) {
        val path = PathSaver().getVideosDownloadPath(contexts)
        MainActivity().createSingleDirectory(path.toString())
        try {
            var forToast: String
            val py = Python.getInstance()
            val mainFile = py.getModule("main")
            val variable = mainFile["DownloadVideo"]
            CoroutineScope(Dispatchers.IO).launch {
                val tester = variable?.call(link, path)?.toString()
                val result: String = tester ?: "No result from python"
//                createSingleDirectory("/storage/emulated/0/Movies/ForUI")
                forToast = result
                tester?.let {
                    if (File(it).exists()) {
                        MediaScannerConnection.scanFile(
                            contexts,
                            arrayOf(tester),
                            null,
                            null
                        )
                        Log.d("MainActivity", "File scan initiated for $tester")
                        forToast =
                            "$title has been downloaded successfully go check it out!"
                    } else {
                        Log.e("MainActivity", "File does not exist at path: $it")
                        forToast =
                            "Download interrupted by the internet please try again!"

                    }
                }
                withContext(Dispatchers.Main) {
                    showDiagloForDownloading(forToast)
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error: ${e.message}")
            showDiagloForDownloading(
                "An Exception Error from kt: \n$e")
            stopSelf()
        }
    }
    private fun showDiagloForDownloading(inputText: String) {
        Toast.makeText(this, inputText, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

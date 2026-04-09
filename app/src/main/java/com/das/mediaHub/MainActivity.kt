package com.das.mediaHub

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.content.Intent
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TEXT
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.constants.DownloadConstants.DOWNLOAD_FINISHED
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.navigation.NavScreens
import com.das.mediaHub.navigation.NavScreens.Downloaded
import com.das.mediaHub.navigation.NavScreens.OnlineVideoPlayer
import com.das.mediaHub.navigation.NavScreens.Searcher
import com.das.mediaHub.navigation.NavScreens.Setting
import com.das.mediaHub.services.media.LocalBackGroundPlayer
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.theme.DasMediaHubTheme
import com.das.python.YouTuber.extractPlaylistId
import com.das.python.YouTuber.isValidYouTubePlaylistUrl
import com.das.python.YouTuber.isValidYoutubeURL
import com.das.python.YouTuber.youtubeExtractor
import java.io.File


class MainActivity : ComponentActivity() {

    private var pendingIntent by mutableStateOf<Intent?>(null)


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        onNewIntent(intent)
        setContent {
            DasMediaHubTheme {
                MainApp(
                    pendingIntent = pendingIntent,
                    onHandleIntent = { intent, backStack ->
                        backStack.handleNavIntent(intent)
                        pendingIntent = null
                    }
                )
            }
        }
    }



    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingIntent = intent
    }


    override fun onStart() {
        super.onStart()
        permissionsGranted()
    }



    private fun NavBackStack<NavKey>.handleNavIntent(intent: Intent) {
        when (intent.action) {

            Intent.ACTION_SEND -> {
                val type = intent.type ?: return
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




    private fun NavBackStack<NavKey>.newTextIntent(
        sharedText: String
    ) {
        if (sharedText.isValidYoutubeURL()) {
            val videoId = sharedText.youtubeExtractor()
            if (videoId != null) {
                add(
                    OnlineVideoPlayer(
                        videoId = videoId
                    )
                )
            } else {
                add(Searcher(sharedText))
            }

        } else if (sharedText.isValidYouTubePlaylistUrl()) {

            val videoId = extractPlaylistId(sharedText)
            if (videoId != null ) {
                add(
                    OnlineVideoPlayer(videoId = videoId)
                )
            } else {
                add(Searcher(sharedText))
            }

        } else if (sharedText.startsWith("DownloadsPageFr")) {
            add(Downloaded)
        } else {
            add(Searcher(sharedText))
        }
    }

    private fun Intent.newReceivedMediaTypeVideo(backStack: NavBackStack<NavKey>) {

        val videoUri = getParcelableExtra(
            this,
            EXTRA_STREAM,
            Uri::class.java
        )

        if (videoUri != null) {
            backStack.add(NavScreens.LocalVideoPlayer(videoUri.toString()))
        } else {
            showNotificationDialog = TopPopUp(
                message = "Video not found",
                icon = Icons.Default.Info
            )
        }
    }

    private fun Intent.newReceivedMediaTypeAudio() {
        val audioUri = getParcelableExtra(
            this,
            EXTRA_STREAM,
            Uri::class.java
        )

        if (audioUri != null) {
            playAudio(audioUri)
        } else {
            showNotificationDialog = TopPopUp(
                message = "Audio not found",
                icon = Icons.Default.Info
            )
        }
    }

    private fun Uri.newMediaIntent(backStack: NavBackStack<NavKey>) {
        val mimeType = contentResolver.getType(this) ?: ""

        if (mimeType.startsWith("video/")) {
            backStack.add(NavScreens.LocalVideoPlayer(toString()))
        } else if (mimeType.startsWith("audio/")) {
            playAudio(this)
        } else {
            showNotificationDialog = TopPopUp(
                message = "Unsupported media type",
                icon = Icons.Default.Info
            )
        }
    }

    private fun playAudio(uri: Uri?) {
        val playIntent = Intent(this, LocalBackGroundPlayer::class.java).apply {
            action = ACTION_START
            putExtra("media_id", uri?.path)
            putExtra("media_url", uri?.path)
            putExtra("title", title)
        }
        startService(playIntent)
    }


    private fun permissionsGranted() {
        if (SDK_INT >= TIRAMISU) {

            val permissions = arrayOf(
                POST_NOTIFICATIONS,
                READ_MEDIA_VIDEO,
                READ_MEDIA_AUDIO
            )

            val hasAllPermissions = permissions.all {
                checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
            if (!hasAllPermissions) {
                requestPermissions(permissions, 1)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        pendingIntent = null
    }

}

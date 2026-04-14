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
                    pendingIntent = pendingIntent
                ) { intent, backStack ->
                    handleNavIntent(intent = intent, backStack = backStack)
                    pendingIntent = null
                }
            }
        }
    }



    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingIntent = intent
    }


    override fun onStart() {
        super.onStart()
        requestMediaPermissionsIfNeeded()
    }



    private fun handleNavIntent(intent: Intent, backStack: NavBackStack<NavKey>) {
        when (intent.action) {
            Intent.ACTION_SEND -> handleSendIntent(intent, backStack)
            Intent.ACTION_VIEW -> handleViewIntent(intent, backStack)
            DOWNLOAD_FINISHED -> handleDownloadFinished(intent)
            Intent.ACTION_APPLICATION_PREFERENCES -> backStack.add(Setting)
        }
    }


    private fun handleSendIntent(
        intent: Intent,
        backStack: NavBackStack<NavKey>
    ) {
        val type = intent.type.orEmpty()

        when {
            type.startsWith("text/") -> {
                val sharedText = intent.getStringExtra(EXTRA_TEXT).orEmpty()
                handleSharedText(sharedText, backStack)
            }

            type.startsWith("video/") -> {
                val uri = intent.getSharedUri()
                if (uri != null) {
                    backStack.add(NavScreens.LocalVideoPlayer(uri.toString()))
                } else {
                    showInfoMessage("Video not found")
                }
            }

            type.startsWith("audio/") -> {
                val uri = intent.getSharedUri()
                if (uri != null) {
                    playAudio(uri)
                } else {
                    showInfoMessage("Audio not found")
                }
            }

            else -> showInfoMessage("Unsupported shared content")
        }
    }

    private fun handleViewIntent(
        intent: Intent,
        backStack: NavBackStack<NavKey>
    ) {
        val uri = intent.data
        if (uri == null) {
            showInfoMessage("Media not found")
            return
        }

        when {
            isVideoUri(uri) -> backStack.add(NavScreens.LocalVideoPlayer(uri.toString()))
            isAudioUri(uri) -> playAudio(uri)
            else -> showInfoMessage("Unsupported media type")
        }
    }

    private fun handleSharedText(
        sharedText: String,
        backStack: NavBackStack<NavKey>
    ) {
        when {
            sharedText.startsWith("DownloadsPageFr") -> {
                backStack.add(Downloaded)
            }

            sharedText.isValidYoutubeURL() -> {
                val videoId = sharedText.youtubeExtractor()
                if (videoId != null) {
                    backStack.add(OnlineVideoPlayer(videoId))
                } else {
                    backStack.add(Searcher(sharedText))
                }
            }

            sharedText.isValidYouTubePlaylistUrl() -> {
                val playlistId = extractPlaylistId(sharedText)
                if (playlistId != null) {
                    backStack.add(OnlineVideoPlayer(playlistId))
                } else {
                    backStack.add(Searcher(sharedText))
                }
            }

            else -> {
                backStack.add(Searcher(sharedText))
            }
        }
    }

    private fun handleDownloadFinished(intent: Intent) {
        val apkPath = intent.getStringExtra("apk_path") ?: return
        installApk(File(apkPath))
    }

    private fun installApk(file: File) {

        val apkUri = FileProvider.getUriForFile(
            this@MainActivity,
            "${this@MainActivity.packageName}.file-provider",
            file
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivity(installIntent)
    }


    private fun Intent.getSharedUri(): Uri? {
        return getParcelableExtra(this, EXTRA_STREAM, Uri::class.java)
    }

    private fun isVideoUri(uri: Uri): Boolean {
        return contentResolver.getType(uri).orEmpty().startsWith("video/")
    }

    private fun isAudioUri(uri: Uri): Boolean {
        return contentResolver.getType(uri).orEmpty().startsWith("audio/")
    }

    private fun showInfoMessage(message: String) {
        showNotificationDialog = TopPopUp(
            message = message,
            icon = Icons.Default.Info
        )
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


    private fun requestMediaPermissionsIfNeeded() {
        if (SDK_INT < TIRAMISU) return

        val permissions = arrayOf(
            POST_NOTIFICATIONS,
            READ_MEDIA_VIDEO,
            READ_MEDIA_AUDIO
        )

        val missingPermissions = permissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissions(missingPermissions.toTypedArray(), 1)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        pendingIntent = null
    }

}

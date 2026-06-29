package com.das.mediaHub

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.content.Intent
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TEXT
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.constants.DownloadConstants.DOWNLOAD_FINISHED
import com.das.mediaHub.data.constants.Notifications.OPEN_IT_NOW
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.navigation.AppBackStack
import com.das.mediaHub.navigation.Destination
import com.das.mediaHub.navigation.Destination.Downloaded
import com.das.mediaHub.navigation.Destination.OnlineVideoPlayer
import com.das.mediaHub.navigation.Destination.Searcher
import com.das.mediaHub.navigation.Destination.Setting
import com.das.mediaHub.network.MainViewModel
import com.das.mediaHub.services.media.local.LocalBackGroundPlayer
import com.das.mediaHub.ui.notification.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.theme.DasMediaHubTheme
import com.das.python.YouTuber.extractPlaylistId
import com.das.python.YouTuber.isValidYouTubePlaylistUrl
import com.das.python.YouTuber.isValidYoutubeURL
import com.das.python.YouTuber.youtubeExtractor
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingIntent: Intent? = null

    @Inject
    lateinit var justExoPlayer: ExoPlayer

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        updateNewIntent(intent)
        setContent {
            var updateApkFile by retain {
                mutableStateOf<File?>(null)
            }
            val showUpdateDialog by viewModel.showUpdateDialog.collectAsStateWithLifecycle()

            val networkState by viewModel.networkState.collectAsStateWithLifecycle()
            val pendingUpdate by viewModel.pendingUpdate.collectAsStateWithLifecycle()

            LaunchedEffect(pendingUpdate) {
                val path = pendingUpdate.apkPath ?: return@LaunchedEffect

                val file = File(path)

                if (file.exists() && pendingUpdate.versionCode > BuildConfig.VERSION_CODE) {
                    updateApkFile = file
                    viewModel.setShowUpdateDialog(true)
                } else if (file.exists()) {
                    file.delete()
                    viewModel.clearPendingUpdate()
                }
            }

            DasMediaHubTheme {
                MainApp(
                    status = networkState,
                    pendingIntent = pendingIntent,
                    onHandleIntent = { intent, backStack ->
                        backStack.handleNavIntent(intent = intent)
                    },
                    openNetworkSetting = {
                        handleNetwork()
                    },
                    onShowMainUpdateDialog = {
                        viewModel.setShowUpdateDialog(true)
                    }
                )

                if (showUpdateDialog) {
                    DasUpdateDialog(
                        onInstall = {
                            updateApkFile?.let { installApk(it) }
                            viewModel.setShowUpdateDialog(false)
                        },
                        notNow = {
                            viewModel.setShowUpdateDialog(false)
                        },
                        onCancel = {
                            updateApkFile?.delete()
                            updateApkFile = null
                            viewModel.clearPendingUpdate()
                            viewModel.setShowUpdateDialog(false)
                        }
                    )
                }
            }
        }
    }
    @Composable
    private fun DasUpdateDialog(
        onInstall: () -> Unit,
        notNow: () -> Unit,
        onCancel: () -> Unit
    ) {
        Dialog(onDismissRequest = { /* locked */ }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(min = 280.dp)
                ) {

                    Text(
                        text = "Update available",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "A new version has finished downloading and is ready to install.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(20.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Improved performance and bug fixes")
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Installation takes less than a minute")
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Secondary actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onCancel) {
                            Text("No")
                        }

                        TextButton(onClick = notNow) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Later")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Primary action
                    Button(
                        onClick = onInstall,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.InstallMobile,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Install update")
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        updateNewIntent(intent)
    }
    private fun updateNewIntent(newIntent: Intent?) {
        pendingIntent = newIntent
    }


    override fun onStart() {
        super.onStart()
        requestMediaPermissionsIfNeeded()
    }



    private fun AppBackStack.handleNavIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> handleSendIntent(intent)
            Intent.ACTION_VIEW -> handleViewIntent(intent)
            DOWNLOAD_FINISHED -> handleDownloadFinished(intent)
            Intent.ACTION_APPLICATION_PREFERENCES -> add(Setting)
            OPEN_IT_NOW -> {
                intent.getStringExtra("VideoID")?. let {
                    add(
                        OnlineVideoPlayer(
                            it
                        )
                    )
                }
            }
        }
        updateNewIntent(null)
    }


    private fun AppBackStack.handleSendIntent(
        intent: Intent
    ) {
        val type = intent.type.orEmpty()

        when {
            type.startsWith("text/") -> {
                val sharedText = intent.getStringExtra(EXTRA_TEXT).orEmpty()
                handleSharedText(sharedText)
            }

            type.startsWith("video/") -> {
                val uri = intent.getSharedUri()
                if (uri != null) {
                    add(Destination.LocalVideoPlayer(uri.toString()))
                } else {
                    showInfoMessage("Video not found")
                }
            }

            type.startsWith("audio/") -> {
                val uri = intent.getSharedUri()
                if (uri != null) {
                    uri.path?.let {
                        playAudio(it)
                    }
                } else {
                    showInfoMessage("Audio not found")
                }
            }

            else -> showInfoMessage("Unsupported shared content")
        }
    }

    private fun AppBackStack.handleViewIntent(
        intent: Intent
    ) {
        val uri = intent.data
        if (uri == null) {
            showInfoMessage("Media not found")
            return
        }

        when {
            isVideoUri(uri) -> add(Destination.LocalVideoPlayer(uri.toString()))
            isAudioUri(uri) -> {
                uri.path?.let {
                    playAudio(it)
                }
            }

            else -> showInfoMessage("Unsupported media type")
        }
    }

    private fun AppBackStack.handleSharedText(
        sharedText: String
    ) {
        when {
            sharedText.startsWith("DownloadsPageFr") -> {
                add(Downloaded)
            }

            sharedText.isValidYoutubeURL() -> {
                val videoId = sharedText.youtubeExtractor()
                if (videoId != null) {
                    add(OnlineVideoPlayer(videoId))
                } else {
                    add(Searcher(sharedText))
                }
            }

            sharedText.isValidYouTubePlaylistUrl() -> {
                val playlistId = extractPlaylistId(sharedText)
                if (playlistId != null) {
                    add(OnlineVideoPlayer(playlistId))
                } else {
                    add(Searcher(sharedText))
                }
            }

            else -> {
                add(Searcher(sharedText))
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
            "${this@MainActivity.packageName}.provider",
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

    private fun playAudio(uri: String) {

        justExoPlayer.setMediaItem(MediaItem.fromUri(uri))
        justExoPlayer.prepare()
        val playIntent = Intent(this, LocalBackGroundPlayer::class.java).apply {
            action = ACTION_START
            putExtra("media_id", uri)
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

    private fun handleNetwork() {
        val intent = if (SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        } else {
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        }

        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateNewIntent(null)
    }

}
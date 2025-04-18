package com.das.forui.ui.videoPlayerLocally

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.PowerManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.das.forui.MainActivity
import com.das.forui.databased.PathSaver.getVideosDownloadPath
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import androidx.navigation.NavController
import com.das.forui.mediacontroller.VideoPlayerControllers.PlayerControls
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File










@SuppressLint("UnsafeOptInUsageError", "SourceLockedOrientationActivity")
@Composable
fun ExoPlayerUI(navController: NavController, videoUri: String) {

    val mContext = LocalContext.current
    val activity = mContext as? Activity
    val powerManager = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    @Suppress("DEPRECATION")
    val wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp:VideoPlayer")

    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

    val exoMetadata = MediaMetadata.Builder()
        .setTitle("mediaTitle")
        .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
        .build()

    val mediaItem = MediaItem.Builder()
        .setMediaId(videoUri)
        .setUri(videoUri)
        .setMediaMetadata(exoMetadata)
        .build()


    val mExoPlayer = remember(mContext) {
        ExoPlayer.Builder(mContext).build().apply {
            setMediaItem(mediaItem)
            playWhenReady = true
            prepare()
            MainActivity().requestAudioFocusFromMain(mContext, this)
        }
    }
    mExoPlayer.addMediaItems(
        fetchDataFromDatabase(
            getVideosDownloadPath(mContext),
            mediaItem.mediaMetadata.title.toString()
        )
    )

    var controlsVisible by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    fun startAutoHideTimer() {
        coroutineScope.launch {
            delay(4000) // 3 seconds
            controlsVisible = false
        }
    }


    LaunchedEffect(Unit) {
        startAutoHideTimer()
    }

    val presentationState = rememberPresentationState(mExoPlayer)

    LaunchedEffect(mExoPlayer.isPlaying) {
        if (mExoPlayer.isPlaying) {
            val videoDurationMs = mExoPlayer.duration / 1000
            if (!wakeLock.isHeld) {
                wakeLock.acquire(videoDurationMs)
            }
        } else {
            // Set default timeout (e.g., 10 minutes) when video is paused or stopped
            if (!wakeLock.isHeld) {
                wakeLock.acquire(2 * 60 * 1000L) // 10 minutes
            }
        }
    }




    // Ensure the ExoPlayer is released when composable is disposed

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValue ->

        val scaledModifier = Modifier
            .fillMaxSize()
            .padding(paddingValue)
            .resizeWithContentScale(ContentScale.Fit, presentationState.videoSizeDp)
            .pointerInput(Unit) {
                detectTapGestures {
                    if (controlsVisible) {
                        controlsVisible = false // hide immediately
                    } else {
                        controlsVisible = true
                        startAutoHideTimer()
                    }
                }
            }


        PlayerSurface(
            mExoPlayer,
            surfaceType = SURFACE_TYPE_SURFACE_VIEW,
            modifier = scaledModifier
        )

        if (presentationState.coverSurface) {
            // Cover the surface that is being prepared with a shutter
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black))
        }

        PlayerControls(
            mExoPlayer = mExoPlayer,
            isVisible ={controlsVisible},
            navigateUp = {
                navController.navigateUp()
            }
        )

    }

    DisposableEffect(mExoPlayer) {
        onDispose {
            mExoPlayer.release()
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

}









private fun fetchDataFromDatabase(
    pathLocation: String,
    currentMediaTitle: String
): MutableList<MediaItem> {
    val fileLists = mutableListOf<MediaItem>().apply {
        clear()
    }
    val pathOfVideos = File(pathLocation)
    if (pathOfVideos.exists()) {
        val fileNames = arrayOfNulls<String>(pathOfVideos.listFiles()!!.size)
        val pathOfVideosUris = arrayOfNulls<Uri?>(pathOfVideos.listFiles()!!.size)
        pathOfVideos.listFiles()!!.mapIndexed { index, item ->
            fileNames[index] = item?.name
            pathOfVideosUris[index] = item?.toUri()

        }
        fileNames.zip(pathOfVideosUris).forEach { (fileName, videoUri) ->
            if (videoUri != null && fileName != null && currentMediaTitle != fileName) {
                val exoMetadata = MediaMetadata.Builder()
                    .setTitle(fileName)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                    .build()

                fileLists.add(
                    MediaItem.Builder()
                        .setMediaId(videoUri.toString())
                        .setUri(videoUri)
                        .setMediaMetadata(exoMetadata)
                        .build()
                )
            }
        }
    }
    return fileLists

}
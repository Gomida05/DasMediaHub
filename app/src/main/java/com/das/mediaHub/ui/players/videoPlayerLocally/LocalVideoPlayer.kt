package com.das.mediaHub.ui.players.videoPlayerLocally

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.das.mediaHub.PIP.shouldEnterPipMode
import com.das.mediaHub.WakeLockHelper
import com.das.mediaHub.mediacontroller.LocalVideoListener
import com.das.mediaHub.player.video.LocalVideoManger
import com.das.mediaHub.ui.players.videoPlayer.CustomMethods.setFullscreen


@SuppressLint("UnsafeOptInUsageError")
@Composable
fun LocalVideoPlayer(videoUri: String) {

    val viewModel = viewModel<LocalPlayerViewModel>()

    val isError by viewModel.errorFound
    val mediaItems by viewModel.mediaItems

    val mContext = LocalContext.current
    val activity = LocalActivity.current

    val exoMetadata = remember {
        MediaMetadata.Builder()
            .setTitle("mediaTitle")
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .build()
    }

    val mediaItem = remember {
        MediaItem.Builder()
            .setMediaId(videoUri)
            .setUri(videoUri)
            .setMediaMetadata(exoMetadata)
            .build()
    }



    val manager = remember {
        LocalVideoManger(
            mContext,
            LocalVideoListener(activity)
        ).apply {
            addListener()
            playVideo(mediaItem)
        }
    }

    val mExoPlayer = remember {
        manager.player
    }

    LaunchedEffect(Unit) {
        activity?.setFullscreen(true)
        shouldEnterPipMode = true
        WakeLockHelper.releaseWakeLock(activity)
        viewModel.loadItems(mediaItem.mediaMetadata.title.toString())
    }

    if (mediaItems.isNotEmpty()){
        LaunchedEffect(Unit) {
            manager.addMediaItems(mediaItems)
        }
    }

    val playerView = remember {
        PlayerView(mContext).apply {
            player = mExoPlayer
            useController = true
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
    }

    Scaffold { innerPadding ->
        if (!isError.isNullOrEmpty()){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = isError.toString(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        AndroidView(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Black)
                .zIndex(0f),
            factory = { playerView },
            update = { it.player = mExoPlayer }
        )
    }


    DisposableEffect(Unit) {
        onDispose {
            manager.release()
            WakeLockHelper.releaseWakeLock(activity)
            shouldEnterPipMode = false
            activity?.setFullscreen(false)
        }
    }

}


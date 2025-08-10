package com.das.mediaHub.ui.downloads.videoPlayerLocally

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.LayoutInflater
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.das.mediaHub.MainActivity
import com.das.mediaHub.data.databased.PathSaver.getVideosDownloadPath
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.das.mediaHub.PIP.shouldEnterPipMode
import com.das.mediaHub.R
import com.das.mediaHub.WakeLockHelper
import java.io.File


@Composable
fun LocalVideoPlayer(videoUri: String) {

    val mContext = LocalContext.current
    val activity = LocalActivity.current

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

    LaunchedEffect(mExoPlayer.isPlaying) {
        if (mExoPlayer.isPlaying) {
            WakeLockHelper.acquireWakeLock(activity)
            shouldEnterPipMode = true
        } else {
            shouldEnterPipMode = false
            WakeLockHelper.releaseWakeLock(activity)
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        factory = { context ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.video_player_ui, null, false) as PlayerView

            view.player = mExoPlayer
            view.useController = true
            view
        },
        update = { playerView ->
            playerView.player = mExoPlayer
        }
    )


    DisposableEffect(mExoPlayer) {

        onDispose {
            mExoPlayer.release()
            WakeLockHelper.releaseWakeLock(activity)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            setFullscreen(activity, false)
        }
    }

}






fun setFullscreen(activity: Activity?, fullscreen: Boolean) {

    activity?.let {
        WindowCompat.setDecorFitsSystemWindows(it.window, !fullscreen)
        val controller = WindowInsetsControllerCompat(it.window, it.window.decorView)

        if (fullscreen) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
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
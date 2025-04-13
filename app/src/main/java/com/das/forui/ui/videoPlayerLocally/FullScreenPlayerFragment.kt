package com.das.forui.ui.videoPlayerLocally

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.das.forui.MainActivity
import com.das.forui.databased.PathSaver.getVideosDownloadPath
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File










@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun ExoPlayerUI(videoUri: String) {

    val mContext = LocalContext.current
    val activity = mContext as? Activity

    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

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


    // Ensure the ExoPlayer is released when composable is disposed
    DisposableEffect(mExoPlayer) {
        onDispose {
            mExoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValue ->


        AndroidView(
            factory = { context ->

                PlayerView(context).apply {
                    player = mExoPlayer
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
        )
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
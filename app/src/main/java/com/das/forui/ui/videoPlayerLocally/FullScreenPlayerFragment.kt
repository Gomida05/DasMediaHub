@file:Suppress("DEPRECATION")
package com.das.forui.ui.videoPlayerLocally

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.das.forui.MainActivity
import com.das.forui.databased.PathSaver
import com.das.forui.objectsAndData.ForUIKeyWords.MEDIA_TITLE
import com.das.forui.objectsAndData.ForUIKeyWords.PLAY_HERE_VIDEO
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.ui.StyledPlayerView
import java.io.File


class FullScreenPlayerFragment: Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return ComposeView(requireContext()).apply {
            val videoUri = arguments?.getString(PLAY_HERE_VIDEO)
            val title = arguments?.getString(MEDIA_TITLE)
            setViewCompositionStrategy(ViewCompositionStrategy.Default)
            setContent {
                val exoMetadata = MediaMetadata.Builder()
                    .setTitle(title)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setMediaId(videoUri.toString())
                    .setUri(videoUri)
                    .setMediaMetadata(exoMetadata)
                    .build()
                ExoPlayerUI(mediaItem)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideBottomNav()


        hideSystemUI()
        (activity as MainActivity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR


    }

    @Composable
    private fun ExoPlayerUI(mediaItem: MediaItem) {
        val mContext = LocalContext.current

        val mExoPlayer = remember(mContext) {
            ExoPlayer.Builder(mContext).build().apply {
                setMediaItem(mediaItem)
                playWhenReady = true
                prepare()
                MainActivity().requestAudioFocusFromMain(requireContext(), this)
            }
        }
        mExoPlayer.addMediaItems(
            fetchDataFromDatabase(
                PathSaver().getVideosDownloadPath(mContext),
                mediaItem.mediaMetadata.title.toString()
            )
        )

        // Ensure the ExoPlayer is released when composable is disposed
        DisposableEffect(mExoPlayer) {
            onDispose {
                mExoPlayer.release()
                showSystemUI()
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }


        AndroidView(factory = { context ->
            StyledPlayerView(context).apply {
                player = mExoPlayer
            }
        })

    }





    private fun hideSystemUI() {


        (activity as MainActivity).window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

    }


    private fun showSystemUI() {
        
        (activity as MainActivity).window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
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



    override fun onDestroy() {
        super.onDestroy()

        (activity as MainActivity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

//        exoPlayer?.let {
//            it.stop()
//            it.release()
//        }
    }
}
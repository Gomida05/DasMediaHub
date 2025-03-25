@file:Suppress("DEPRECATION")
package com.das.forui.ui.videoPlayerLocally


import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.das.forui.MainActivity
import com.das.forui.databased.PathSaver
import com.das.forui.databinding.FullScreenPlayerBinding
import com.das.forui.objectsAndData.ForUIKeyWords.MEDIA_TITLE
import com.das.forui.objectsAndData.ForUIKeyWords.PLAY_HERE_VIDEO
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import java.io.File


class FullScreenPlayerFragment: Fragment() {
    private var _binding: FullScreenPlayerBinding? = null
    private val binding get() = _binding!!
    private var exoPlayer: ExoPlayer? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FullScreenPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideBottomNav()
        exoPlayer?.release()
        exoPlayer = ExoPlayer.Builder(requireContext()).build()



        val videoUri = arguments?.getString(PLAY_HERE_VIDEO)
        val title = arguments?.getString(MEDIA_TITLE)

        if (!videoUri.isNullOrEmpty()){
            (activity as MainActivity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            val exoMetadata = MediaMetadata.Builder()
                .setTitle(title)
                .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaId(videoUri.toString())
                .setUri(videoUri)
                .setMediaMetadata(exoMetadata)
                .build()

            playVideo(mediaItem)
        }


        activity?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                exoPlayer?.let {
                    it.stop()
                    it.release()
                }

                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                showSystemUI()
                findNavController().navigateUp()
            }
        })
    }




    private fun playVideo( mediaItem: MediaItem) {
        hideSystemUI()

        val playerView = binding.startVideoPlayerLocally

        playerView.player = exoPlayer
        playerView.keepScreenOn = true
        exoPlayer?.let {
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
        }
        exoPlayer?.addMediaItems(
            fetchDataFromDatabase(
                PathSaver().getVideosDownloadPath(
                    requireContext()))
        )
        MainActivity().requestAudioFocusFromMain(requireContext(), exoPlayer)
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
        pathLocation: String
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
                if (videoUri != null && fileName != null) {
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


    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        binding.startVideoPlayerLocally.useController = !isInPictureInPictureMode
    }

    override fun onDestroy() {
        super.onDestroy()

        (activity as MainActivity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        exoPlayer?.let {
            it.stop()
            it.release()
        }
    }
}
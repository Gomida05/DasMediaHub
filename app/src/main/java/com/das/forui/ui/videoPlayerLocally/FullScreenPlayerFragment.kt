@file:Suppress("DEPRECATION", "SourceLockedOrientationActivity")
package com.das.forui.ui.videoPlayerLocally

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.das.forui.MainActivity
import com.das.forui.MainActivity.Youtuber.PLAY_HERE_AUDIO
import com.das.forui.MainActivity.Youtuber.PLAY_HERE_VIDEO
import com.das.forui.R
import com.das.forui.databinding.FullScreenPlayerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.fromUri


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
        exoPlayer = ExoPlayer.Builder(requireContext()).build()

        (activity as MainActivity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        val videoUri = arguments?.getString(PLAY_HERE_VIDEO)
        val audioUri = arguments?.getString(PLAY_HERE_AUDIO)
        if (!videoUri.isNullOrEmpty()){
            playVideo(
                fromUri(videoUri.toUri())
            )
        }
        else if(!audioUri.isNullOrEmpty()){

            playAudio(
                fromUri(audioUri.toUri())
            )
        }


        view.findViewById<ImageButton>(R.id.exo_of_fullscreen).setOnClickListener{
            Toast.makeText(requireContext(), "coming soon!", Toast.LENGTH_SHORT).show()
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
        MainActivity().requestAudioFocusFromMain(requireContext(), exoPlayer)
    }

    private fun playAudio(mediaItem: MediaItem) {
        showSystemUI()
        val playerView = binding.startVideoPlayerLocally
        playerView.player = exoPlayer
        playerView.setBackgroundResource(R.drawable.music_note_24dp)
        playerView.keepScreenOn = false
        exoPlayer?.let {
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
        }
        MainActivity().requestAudioFocusFromMain(requireContext(), exoPlayer)
    }




    private fun hideSystemUI() {

        
        activity?.window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

    }


    private fun showSystemUI() {
        
        (activity as MainActivity).window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
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
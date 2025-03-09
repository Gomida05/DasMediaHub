package com.das.forui.ui.videoPlayerLocally

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity.AUDIO_SERVICE
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
    @Suppress("DEPRECATION")
    private var exoPlayer: ExoPlayer? = null

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioManager.OnAudioFocusChangeListener? =null


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
        @Suppress("DEPRECATION")
        exoPlayer = ExoPlayer.Builder(requireContext()).build()

//        @SuppressLint("SourceLockedOrientationActivity")
        (activity as MainActivity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        audioManager = requireContext().getSystemService(AUDIO_SERVICE) as AudioManager
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

                @SuppressLint("SourceLockedOrientationActivity")
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                showSystemUI()
                findNavController().navigateUp()
            }
        })
    }




    private fun playVideo(@Suppress("DEPRECATION") mediaItem: MediaItem) {
        hideSystemUI()
        val playerView = binding.startVideoPlayerLocally
        playerView.player = exoPlayer
        playerView.keepScreenOn = true
        exoPlayer?.let {
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
        }
        requestAudioFocus(exoPlayer)
    }

    private fun playAudio(@Suppress("DEPRECATION") mediaItem: MediaItem) {
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
        requestAudioFocus(exoPlayer)
    }


    private fun requestAudioFocus(@Suppress("DEPRECATION") exoPlayer: ExoPlayer?) {
        audioFocusRequest = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    // Pause playback when losing focus
                    exoPlayer?.playWhenReady = false
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // Resume playback when gaining focus
                    exoPlayer?.playWhenReady = true
                    exoPlayer?.volume = 1.0f
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // Pause temporarily (e.g., during a phone call)
                    exoPlayer?.playWhenReady = false
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    // Can continue playing but with lower volume
                    exoPlayer?.volume = 0.1f  // Reduce volume
                }
            }
        }

        // Request audio focus when app starts
        @Suppress("DEPRECATION")
        val result = audioManager.requestAudioFocus(
            audioFocusRequest!!,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Handle audio focus failure (e.g., another app already has focus)
        }
    }

    private fun hideSystemUI() {

        @Suppress("DEPRECATION")
        activity?.window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

    }


    private fun showSystemUI() {
        @Suppress("DEPRECATION")
        (activity as MainActivity).window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }


    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        binding.startVideoPlayerLocally.useController = !isInPictureInPictureMode
    }

    override fun onDestroy() {
        super.onDestroy()

        @SuppressLint("SourceLockedOrientationActivity")
        (activity as MainActivity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        exoPlayer?.let {
            it.stop()
            it.release()
        }
    }
}
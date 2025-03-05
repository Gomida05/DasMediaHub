@file:Suppress("DEPRECATION")

package com.das.forui

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.das.forui.databinding.FullScreenPlayerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView


class FullScreenPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    var exoPlayerOfFullScreen: ExoPlayer?= null
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioManager.OnAudioFocusChangeListener? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = FullScreenPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager

        playerView = findViewById(R.id.start_video_player_locally)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
        exoPlayerOfFullScreen = ExoPlayer.Builder(this).build()
        onNewIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        findViewById<ImageButton>(R.id.exo_of_fullscreen).setOnClickListener{
            showMyDialog("coming soon")
        }

        this.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                exoPlayerOfFullScreen?.stop()
                val intent = Intent(this@FullScreenPlayerActivity, MainActivity::class.java)
                startActivity(intent)
            }
        })
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_VIEW) {
            val mediaUri: Uri? = intent.data
            mediaUri?.let {
                val mimeType = contentResolver.getType(it) ?: ""
                if (mimeType.startsWith("video/")) {
                    playVideo(it)
                } else if (mimeType.startsWith("audio/")) {
                    playAudio(it)
                } else {
                    showMyDialog("Unsupported media type")
                }
            }
        } else if(intent.action == "ControlMedia"){
            if (intent.getStringExtra("ControlMedia") == "play"){
                exoPlayerOfFullScreen?.play()
            }
            else{
                exoPlayerOfFullScreen?.pause()
            }

        }
        else {
            showMyDialog("No valid media to play")
        }
    }



    private fun playVideo(videoUri: Uri) {
        println("Playing video from URI: $videoUri")
        val mediaItem = MediaItem.fromUri(videoUri)
        playerView.player = exoPlayerOfFullScreen
        exoPlayerOfFullScreen?.setMediaItem(mediaItem)
        exoPlayerOfFullScreen?.prepare()
        requestAudioFocus()
        exoPlayerOfFullScreen?.play()
    }

    private fun playAudio(audioUri: Uri) {
        println("Playing audio from URI: $audioUri")

        val mediaItem = MediaItem.fromUri(audioUri)
        playerView.player = exoPlayerOfFullScreen
        playerView.setBackgroundResource(R.drawable.music_note_24dp)
        playerView.keepScreenOn = false
        exoPlayerOfFullScreen?.setMediaItem(mediaItem)
        requestAudioFocus()
        exoPlayerOfFullScreen?.prepare()
        exoPlayerOfFullScreen?.play()
    }





    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val playIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent("com.das.forui.ACTION_CONTROL_MEDIA").apply {
                    putExtra("ControlMedia", "play")  // Set the action as "play"
                },
                PendingIntent.FLAG_IMMUTABLE
            )

            val pauseIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent("com.das.forui.ACTION_CONTROL_MEDIA").apply {
                    putExtra("ControlMedia", "pause")  // Set the action as "pause"
                },
                PendingIntent.FLAG_IMMUTABLE
            )


            val playAction = RemoteAction(
                Icon.createWithResource(this, R.drawable.play_arrow_24dp),  // Icon for Play
                "Play",  // Title
                "Resume playback",  // Description
                playIntent  // PendingIntent
            )

            val pauseAction = RemoteAction(
                Icon.createWithResource(this, R.drawable.pause_icon),  // Icon for Pause
                "Pause",  // Title
                "Pause playback",  // Description
                pauseIntent  // PendingIntent
            )



            val aspectRatio = Rational(14, 9)

            val pipBuilder = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .setActions(listOf(playAction,pauseAction))
            findViewById<PlayerView>(R.id.start_video_player_locally).useController = false
            enterPictureInPictureMode(pipBuilder.build())
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        findViewById<PlayerView>(R.id.start_video_player_locally).useController = !isInPictureInPictureMode
    }






    private fun showMyDialog(inputText: String) {
        Toast.makeText(this, inputText, Toast.LENGTH_SHORT).show()
    }



    private fun requestAudioFocus() {
        audioFocusRequest = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    // Pause playback when losing focus
                    exoPlayerOfFullScreen?.playWhenReady = false
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // Resume playback when gaining focus
                    exoPlayerOfFullScreen?.playWhenReady = true
                    exoPlayerOfFullScreen?.volume = 1.0f
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // Pause temporarily (e.g., during a phone call)
                    exoPlayerOfFullScreen?.playWhenReady = false
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    // Can continue playing but with lower volume
                    exoPlayerOfFullScreen?.volume = 0.1f  // Reduce volume
                }
            }
        }

        // Request audio focus when app starts
        val result = audioManager.requestAudioFocus(
            audioFocusRequest!!,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Handle audio focus failure (e.g., another app already has focus)
        }
    }


}



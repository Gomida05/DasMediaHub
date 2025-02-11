@file:Suppress("DEPRECATION")

package com.das.forui



import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
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
    private var exoPlayer: ExoPlayer?= null
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
        onNewIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        findViewById<ImageButton>(R.id.exo_of_fullscreen).setOnClickListener{
            showMyDiaglo("coming soon")
        }

        this.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                exoPlayer?.stop()
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
                    showMyDiaglo("Unsupported media type")
                }
            }
        } else {
            showMyDiaglo("No valid media to play")
        }
    }



    private fun playVideo(videoUri: Uri) {
        println("Playing video from URI: $videoUri")
        exoPlayer = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(videoUri)
        playerView.player = exoPlayer
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        requestAudioFocus()
        exoPlayer?.play()
    }

    private fun playAudio(audioUri: Uri) {
        println("Playing audio from URI: $audioUri")
        exoPlayer = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(audioUri)
        playerView.player = exoPlayer
        playerView.setBackgroundResource(R.drawable.music_note_24dp)
        playerView.keepScreenOn = false
        exoPlayer?.setMediaItem(mediaItem)
        requestAudioFocus()
        exoPlayer?.prepare()
        exoPlayer?.play()
    }





    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(14, 9)

            val pipBuilder = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
            findViewById<PlayerView>(R.id.start_video_player_locally).useController = false
            enterPictureInPictureMode(pipBuilder.build())
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if(isInPictureInPictureMode){
            findViewById<PlayerView>(R.id.start_video_player_locally).useController=false
        }
    }






    private fun showMyDiaglo(inputText: String) {
        Toast.makeText(this, inputText, Toast.LENGTH_SHORT).show()
    }



    private fun requestAudioFocus() {
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
        val result = audioManager.requestAudioFocus(
            audioFocusRequest!!,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Handle audio focus failure (e.g., another app already has focus)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}


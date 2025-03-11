@file:Suppress("DEPRECATION")
package com.das.forui.ui.home


import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.das.forui.MainActivity
import com.das.forui.services.MyService
import com.das.forui.R
import com.das.forui.databased.DatabaseFavorite
import com.das.forui.databinding.FragmentHomeBinding
import com.das.forui.services.AudioServiceFromUrl.Companion.ACTION_ADD_TO_WATCH_LATER
import com.das.forui.services.AudioServiceFromUrl.Companion.ACTION_KILL
import com.das.forui.services.AudioServiceFromUrl.Companion.ACTION_NEXT
import com.das.forui.services.AudioServiceFromUrl.Companion.ACTION_PAUSE
import com.das.forui.services.AudioServiceFromUrl.Companion.ACTION_PREVIOUS
import com.das.forui.mediacontroller.NotificationCustomActions
import com.das.forui.mediacontroller.NotificationListenerService
import com.das.forui.mediacontroller.NotificationMediaDescriptionAdapter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var exoPlayer: ExoPlayer? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onStart() {
        super.onStart()

        binding.gosearch.setOnClickListener {
            findNavController().navigate(R.id.nav_searcher)
        }
        binding.profile.setOnClickListener {

            AlertDialog.Builder(context)
                .setTitle("Do you want to start the foregroundService or bannerAds?")
                .setPositiveButton("ForegroundService") { _, _ ->
                    val intent = Intent(requireContext(), MyService::class.java)
                    requireContext().startService(intent)
                }
                .setNegativeButton("BannerAds") { _, _ ->
                    (activity as MainActivity).startBanner(
                        true
                    )
                }
                .setNeutralButton("Test Notification") { _, _ -> testNotification() }
                .show()
        }
        binding.downloadList.setOnClickListener {
            findNavController().navigate(R.id.nav_Downloads)
        }
    }

    @Suppress("DEPRECATION")
    private fun testNotification() {
        exoPlayer?.release()
        val mediaSession = MediaSessionCompat(requireContext(), "AudioService").apply {
            isActive = true
        }
        val mediaUrl =
            "https://rr3---sn-aigl6nzr.googlevideo.com/videoplayback?expire=1741146025&ei=SXPHZ-uiBp_cp-oPmJ6oOA&ip=2a04%3A4a43%3A970f%3Af786%3Ab549%3A3c62%3Aaf7f%3A8463&id=o-AHpFoqcWYooHEtR8fdzMqNg14ZmoWMRt5uOmkTjWYfg5&itag=140&source=youtube&requiressl=yes&xpc=EgVo2aDSNQ%3D%3D&gcr=gb&bui=AUWDL3y9tiVtF8Bfu9EFtaoYPczHvw8c9SHqKTrOJmf8BovfYAf1nBYzf_2N9vAg0PuXDyLisRCYTF4N&spc=RjZbSUIYvd_LsSZKVlOjytWRh-CWPYjHICRGS64xcgFqUJTQ7w&vprv=1&mime=audio%2Fmp4&rqh=1&gir=yes&clen=15664401&dur=967.854&lmt=1726962904598921&keepalive=yes&fexp=24350590,24350602,24350737,24350827,24350961,24351173,24351284,24351341,24351346,51326932&c=ANDROID_VR&txp=5532434&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cxpc%2Cgcr%2Cbui%2Cspc%2Cvprv%2Cmime%2Crqh%2Cgir%2Cclen%2Cdur%2Clmt&sig=AJfQdSswRgIhAOifGjTT0IdRaFmBAZod968stKAZmd__o-5SWjZt12I7AiEAy6eDrkzsOe4MvGGvRRzIahc5jO2v8kmk4Q1RC0FuDcU%3D&redirect_counter=1&cm2rm=sn-uigxx03-ajtl7l&rrc=80&req_id=d7352f1fc7fea3ee&cms_redirect=yes&cmsv=e&met=1741124433,&mh=8E&mm=29&mn=sn-aigl6nzr&ms=rdu&mt=1741123305&mv=u&mvi=3&pl=58&rms=rdu,au&lsparams=met,mh,mm,mn,ms,mv,mvi,pl,rms&lsig=AFVRHeAwRgIhAJIfNqmguexocXYDDKhMK9NN-XYytT2duAVnZckuLrWjAiEAjqIaAGkKDLF_QKytSKVMdrOAoJnhbx-QbcFcV3p6Hbc%3D"
        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUrl))
            prepare()
            play()
        }


        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUrl)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Let her go")
            .putString(
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                "ForUI"
            )
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "unknown album")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayer?.duration!!)
            .build()
        mediaSession.setMetadata(metadata)

        val mediaSessionMe = MediaControllerCompat(requireContext(), mediaSession)

        val notificationMediaNotificationManager =
            PlayerNotificationManager.Builder(requireContext(), 1, "MediaYouTubePlayer")
                .setMediaDescriptionAdapter(
                    NotificationMediaDescriptionAdapter(
                        requireContext(),
                        mediaSessionMe
                    )
                )
                .setChannelImportance(NotificationUtil.IMPORTANCE_HIGH)
                .setCustomActionReceiver(NotificationCustomActions(requireContext()))
                .setNotificationListener(NotificationListenerService(exoPlayer!!))
                .build()

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, exoPlayer?.currentPosition!!,
                    1F
                )
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .addCustomAction(ACTION_PAUSE, "myPauseButton", R.drawable.pause_icon)
                .addCustomAction(ACTION_NEXT, "myNextButton", R.drawable.skip_next_24dp)
                .addCustomAction(ACTION_PREVIOUS, "myPreviousButton", R.drawable.skip_previous_24dp)
                .addCustomAction(
                    ACTION_ADD_TO_WATCH_LATER, "myFavButton",
                    if (DatabaseFavorite(requireContext()).isWatchUrlExist("S9bCLPwzSC0")) R.drawable.favorite else R.drawable.un_favorite_icon
                )
                .addCustomAction(ACTION_KILL, "myStopButton", R.drawable.stop_circle_24dp)
                .setBufferedPosition(exoPlayer?.currentPosition!!)
                .build()
        )

        notificationMediaNotificationManager.setPriority(NotificationCompat.PRIORITY_MAX)
        notificationMediaNotificationManager.setMediaSessionToken(mediaSessionMe.sessionToken)
        notificationMediaNotificationManager.setPlayer(exoPlayer)

    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showBottomNav()
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
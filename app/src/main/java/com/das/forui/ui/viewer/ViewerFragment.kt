@file:Suppress("DEPRECATION")
package com.das.forui.ui.viewer

import android.app.AlertDialog
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chaquo.python.Python
import com.das.forui.services.AudioServiceFromUrl
import com.das.forui.DownloaderClass
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.databased.DatabaseFavorite
import com.das.forui.databinding.VideoViewerBinding
import com.das.forui.ui.viewer.GlobalVideoList.listOfVideos
import com.das.forui.ui.viewer.GlobalVideoList.previousVideos
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@Suppress("SourceLockedOrientationActivity")

class ViewerFragment: Fragment() {
    private lateinit var duration: String
    private lateinit var videoURL: String
    private lateinit var videoTitle: String
    private lateinit var videoID: String
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private lateinit var playerView: PlayerView
    private var _binding: VideoViewerBinding? = null
    var isPlaying = false
    private val binding get() = _binding!!
    private var exoPlayer: ExoPlayer?= null
    private lateinit var gestureDetector: GestureDetector
    private var url= ""
    private var descriptions: String= "Something went wrong please try again!!"
    private var isFullScreen = false
    private val fastForwardInterval: Long = 10000
    private val rewindInterval: Long = 10000
    private lateinit var channelThumbnail: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VideoViewerBinding.inflate(inflater, container, false)
        return  binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listOfVideos.clear()
        (activity as MainActivity).hideBottomNav()
        videoID = arguments?.getString("View_ID").toString()
        playerView = binding.videoPlayerLocally

        playVideo("https://www.youtube.com/watch?v=$videoID")


        val setTitle = view.findViewById<TextView>(R.id.set_title_over_view)


        videoID = arguments?.getString("View_ID").toString()
        videoTitle = arguments?.getString("View_Title").toString()
        videoURL = arguments?.getString("View_URL").toString()
        channelThumbnail = arguments?.getString("channel_Thumbnails").toString()
        var viewNumber = arguments?.getString("View_Number").toString()
        var dateOfVideo = arguments?.getString("dateOfVideo").toString()
        var channelName = arguments?.getString("channelName").toString()
        duration = arguments?.getString("duration").toString()


        val videoPlayerHeight = resources.getDimensionPixelSize(R.dimen.video_player_height)
        binding.hideThese.visibility = View.VISIBLE
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setTitle.visibility = View.GONE
        binding.videoPlayerLocally.layoutParams.height = videoPlayerHeight
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.fullscreen_24dp)
        (activity as MainActivity).findViewById<ImageButton>(R.id.exofullscreen)
            .setImageDrawable(drawable)
        isFullScreen = false
        showSystemUI()


        val buttonSaver = binding.savedToWatchLater
        val dataBase = DatabaseFavorite(requireContext())
        if (dataBase.isWatchUrlExist(videoID)) {
            buttonSaver.tag = "favorite_icon"
            val newIcon = R.drawable.favorite
            val icon = ContextCompat.getDrawable(requireContext(), newIcon)
            buttonSaver.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)
        }



        if (descriptions != "Something went wrong please try again!!") {
            binding.giveMeTitle.text = videoTitle
            setTitle.text = videoTitle
            binding.giveMeViewNumber.text = viewNumber
            binding.giveMeViewChannelName.text = channelName
            binding.giveMeViewDate.text = dateOfVideo
            binding.myComposeView2.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    SuggestedVideos(
                        videoID,
                        videoTitle,
                        channelThumbnail,
                        channelName.toString(),
                        viewNumber.toString(),
                        dateOfVideo.toString()
                    )
                }
            }
            Glide.with(requireContext())
                .load(channelThumbnail)
                .transform(CircleCrop())
                .into(binding.channelImageVideoView)

        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val videoDetails = (activity as MainActivity).callPythonSearchWithLink(videoID)

                withContext(Dispatchers.Main) {
                    if (videoDetails != null) {
                        videoTitle = videoDetails["title"].toString()
                        val views = videoDetails["viewNumber"].toString()
                        val date = videoDetails["date"].toString()
                        val channel = videoDetails["channelName"].toString()
                        descriptions = videoDetails["description"].toString()
                        viewNumber = formatViews(views.toLong())
                        channelName = channel
                        dateOfVideo = formatDate(date)
                        binding.giveMeTitle.text = videoTitle
                        setTitle.text = videoTitle
                        binding.giveMeViewNumber.text = viewNumber
                        binding.giveMeViewChannelName.text = channelName
                        binding.giveMeViewDate.text = dateOfVideo
                        binding.myComposeView2.apply {
                            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                            setContent {
                                SuggestedVideos(
                                    videoID,
                                    videoTitle,
                                    channelThumbnails = channelThumbnail,
                                    channelName,
                                    views,
                                    dateOfVideo
                                )
                            }
                        }
                    }
                }
            }
            Glide.with(requireContext())
                .load(channelThumbnail)
                .transform(CircleCrop())
                .into(binding.channelImageVideoView)

        }


        (activity as MainActivity).onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isFullScreen) {
                        toggleFullScreen()
                    } else {
                        exoPlayer?.playWhenReady = false
                        releaseAudioFocus()
                        findNavController().navigateUp()
                    }
                }
            })




        view.findViewById<ImageButton>(R.id.navigateUpFromVideoPLayer).setOnClickListener {
            if (isFullScreen) {
                toggleFullScreen()
            } else {
                exoPlayer?.stop()
                releaseAudioFocus()
                findNavController().navigateUp()
            }
        }
        view.findViewById<ImageButton>(R.id.exofullscreen).setOnClickListener {
            toggleFullScreen()
        }
        view.findViewById<ImageButton>(R.id.play_next_video).setOnClickListener {
            playThisOne(
                videoDetails = Video(
                    videoId = videoID,
                    title = videoTitle,
                    views = viewNumber,
                    duration = duration,
                    dateOfVideo =  dateOfVideo,
                    channelName = channelName,
                    channelThumbnailsUrl = channelThumbnail
                )
            )
        }
    }


    override fun onStart() {
        super.onStart()



        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    playThisOne()

                    Toast.makeText(requireContext(), "Video end", Toast.LENGTH_SHORT).show()
                }
                super.onPlaybackStateChanged(state)
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                super.onIsLoadingChanged(isLoading)
                val viewIt = view?.findViewById<ProgressBar>(R.id.progress_bar_load_video)
                if (isLoading){
                    viewIt?.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                }
                else{
                    viewIt?.visibility = View.GONE
                    Toast.makeText(requireContext(), "Finished", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onPlayWhenReadyChanged(
                playWhenReady: Boolean,
                reason: Int
            ) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (playWhenReady) {
                    isPlaying = true
                    println("service playing")
                    requestAudioFocus()
                } else {
                    isPlaying = false
                    println("service pausing")
                    releaseAudioFocus()
                }
            }
        })
        gestureDetector =
            GestureDetector(
                requireContext(),
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDown(e: MotionEvent): Boolean {
                        return super.onDown(e)
                    }

                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        handleDoubleTap(e)
                        return super.onDoubleTap(e)
                    }

                })

        // Set OnTouchListener to detect touch events and pass them to the GestureDetector
        playerView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }

            // Pass the event to GestureDetector
            gestureDetector.onTouchEvent(event)
            true
        }



        binding.giveMeTitle.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Descriptions")
                .setMessage(descriptions)
                .setNegativeButton("Close") { _, _ ->

                }
                .show()
        }


        binding.clickForMore.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Do you want to play it in the background?")
                .setPositiveButton("yes") { _, _ ->
                    val playIntent = Intent(requireContext(), AudioServiceFromUrl::class.java)
                    playIntent.action = AudioServiceFromUrl.ACTION_START
                    playIntent.putExtra("videoId", videoID)
                    playIntent.putExtra("media_url", url)
                    playIntent.putExtra("title", binding.giveMeTitle.text.toString())
                    playIntent.putExtra(
                        "channelName",
                        binding.giveMeViewChannelName.text.toString()
                    )
                    playIntent.putExtra("viewNumber", binding.giveMeViewNumber.text.toString())
                    playIntent.putExtra("videoDate", binding.giveMeViewDate.text.toString())
                    playIntent.putExtra("duration", duration)
                    activity?.startService(playIntent)
                }
                .setNegativeButton("No") { _, _ -> }.show()
        }

        binding.channelImageVideoView.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("This feature is currently under development!!!")
                .setPositiveButton("Okay") { _, _ -> }
                .setIcon(R.drawable.setting)
                .setMessage("Thank you for understanding!")
                .setIcon(R.mipmap.ic_launcher)
                .show()
        }





        binding.downloadAsAudio.setOnClickListener {
            DownloaderClass(requireContext()).downloadVideo(url, videoTitle, "mp3")
        }
        binding.downloadAsVideo.setOnClickListener {
            DownloaderClass(requireContext()).downloadVideo(url, videoTitle, "mp4")
//                (activity as MainActivity).downloadVideo(link = videoID, title = videoTitle, contexts = requireContext())
        }

        val buttonSaver = binding.savedToWatchLater
        buttonSaver.setOnClickListener {
            val dbHere = DatabaseFavorite(requireContext())
            val newIcon: Any
            val toastEr: String
            if (buttonSaver.tag == "favorite_icon") {
                dbHere.deleteWatchUrl(videoID)
                buttonSaver.tag = "unfavorite_icon"
                newIcon = R.drawable.un_favorite_icon
                toastEr = "Removed from watch later list"

            } else {
                toastEr = "Added to watch later list"
                val durations = duration
                val videoChannelName =
                    view?.findViewById<TextView>(R.id.give_me_view_channelName)?.text.toString()
                val videoViewCount =
                    view?.findViewById<TextView>(R.id.give_me_view_number)?.text.toString()
                val videoDate =
                    view?.findViewById<TextView>(R.id.give_me_view_date)?.text.toString()
                dbHere.insertData(
                    videoID,
                    videoTitle,
                    videoDate,
                    videoViewCount,
                    videoChannelName,
                    durations
                )
                buttonSaver.tag = "favorite_icon"
                newIcon = R.drawable.favorite
            }
            (activity as MainActivity).showDiaglo(toastEr)
            val icon = ContextCompat.getDrawable(requireContext(), newIcon)
            buttonSaver.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)

//            buttonSaver.tag = if (buttonSaver.tag == "unfavorite_icon") "favorite_icon" else "unfavorite_icon"
        }
        binding.sharingButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${videoURL}&feature=shared")
            }

            val chooser = Intent.createChooser(shareIntent, "Share via")
            startActivity(chooser)
        }
    }


    private fun playThisOne(gotIndex: Int = 1, videoDetails: Video = listOfVideos[gotIndex]){
        exoPlayer?.let {
            it.stop()
            it.release()
        }
        val listOfVideosIndex = videoDetails
        val bundle = Bundle().apply {
            putString("View_ID", listOfVideosIndex.videoId)
            putString("View_URL", "https://www.youtube.com/watch?v=${listOfVideosIndex.videoId}")
            putString("View_Title", listOfVideosIndex.title)
            putString("View_Number", listOfVideosIndex.views)
            putString("dateOfVideo", listOfVideosIndex.dateOfVideo)
            putString("channelName", listOfVideosIndex.channelName)
            putString("duration", listOfVideosIndex.duration)
            putString("channel_Thumbnails", listOfVideosIndex.channelThumbnailsUrl)
        }
        previousVideos.add(listOfVideosIndex)
        findNavController().run {
            popBackStack()
            navigate(R.id.nav_video_viewer, bundle)
        }
    }




    @Composable
    fun SuggestedVideos(
        videoIds: String,
        titleVideo: String, channelThumbnails: String, channelName: String, views: String, dateOfVideo: String
        ) {

        val searchResults = remember { mutableStateOf<List<Video>>(emptyList()) }
        val isLoading = remember { mutableStateOf(true) }



        LaunchedEffect(titleVideo) {
            if (titleVideo.isNotBlank()) {
                isLoading.value = true
                val result = withContext(Dispatchers.IO) {
                    callPythonSearchSuggestion(titleVideo)
                }
                searchResults.value = result ?: emptyList()
                isLoading.value = false
            }
        }


        Scaffold{ paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    if (searchResults.value.isEmpty()) {
                        Text(
                            text = "No results found",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {

                        LazyColumn(modifier = Modifier) {
                            items(searchResults.value) {searchItem ->
                                CategoryItems(
                                    videoId = searchItem.videoId,
                                    titles = searchItem.title,
                                    viewsNumber = searchItem.views,
                                    dateOfVideo = searchItem.dateOfVideo,
                                    channelName = searchItem.channelName,
                                    duration = searchItem.duration,
                                    videoThumbnailURL = searchItem.videoId,
                                    channelThumbnails = searchItem.channelThumbnailsUrl
                                    )
                                listOfVideos.add(searchItem)

                            }
                        }
                    }
                }

            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun CategoryItems(
        videoId: String,
        titles: String,
        viewsNumber: String,
        dateOfVideo: String,
        channelName: String,
        duration: String,
        videoThumbnailURL: String,
        channelThumbnails: String
    ){
        val context= requireContext()


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp, top = 3.dp)
                .combinedClickable(
                    onClick = {
                        playThisOne(
                            videoDetails = Video(
                                videoId = videoId,
                                title = titles,
                                views = viewsNumber,
                                duration = duration,
                                dateOfVideo =  dateOfVideo,
                                channelName = channelName,
                                channelThumbnailsUrl = channelThumbnails
                            )
                        )
                    },
                    onLongClick = {
                        imageViewer(videoId, titles)
                    }
                ),
            shape = RoundedCornerShape(1)
        ) {
            Column (
                modifier = Modifier
                    .height(260.dp)
                    .fillMaxWidth()

            ) {
                Box {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("https://img.youtube.com/vi/$videoThumbnailURL/0.jpg")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Category Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = duration,
                        maxLines = 1,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(40.dp)
                            .padding(end = 3.dp, bottom = 3.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color.DarkGray, RoundedCornerShape(5.dp))
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {


                    IconButton(
                        onClick = {
                            AlertDialog.Builder(context)
                                .setTitle("This feature is currently under development!!!")
                                .setPositiveButton("Okay") { _, _ -> }
                                .setIcon(R.drawable.setting)
                                .setMessage("Thank you for understanding!")
                                .setIcon(R.mipmap.ic_launcher)
                                .show()
                        }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(channelThumbnails)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Category Image",
                            modifier = Modifier
                                .fillMaxSize(),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(
                        modifier = Modifier
                            .width(285.dp)
                            .padding(3.dp)
                    ) {


                        Text(
                            text = titles,
                            maxLines = 1,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 2.dp)
                        )
                        Row {
                            Text(
                                text = channelName,
                                maxLines = 1,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .width(112.dp)
                                    .padding(start = 2.dp)
                            )
                            Text(
                                text = viewsNumber,
                                maxLines = 1,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .width(55.dp)
                                    .padding(start = 5.dp, end = 5.dp)
                            )
                            Text(
                                text = dateOfVideo,
                                maxLines = 1,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .width(100.dp)
                                    .padding(start = 2.dp)
                            )
                        }

                    }
                    IconButton(
                        onClick = {
                            AlertDialog.Builder(context)
                                .setTitle("This feature is currently under development!!!")
                                .setPositiveButton("Okay") { _, _ -> }
                                .setIcon(R.drawable.setting)
                                .setMessage("Thank you for understanding!")
                                .setIcon(R.mipmap.ic_launcher)
                                .show()
                        }

                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.MoreVert),
                            contentDescription = "Back"
                        )
                    }
                }
            }
        }
    }



    private fun playVideo(uri: String) {
        try {

            CoroutineScope(Dispatchers.IO).launch {
                val py = Python.getInstance()
                val mainFile = py.getModule("main")
                val variable = mainFile["get_video_url"]
                val result = variable?.call("https://www.youtube.com/watch?v=$uri").toString()
                if (result != "False" && isAdded) {
                    url = result
                    withContext(Dispatchers.Main) {
                        exoPlayer?.let {
                            it.stop()
                            it.release()
                        }
                        println("Video URL for ExoPlayer: $url")
                        exoPlayer = ExoPlayer.Builder(requireContext()).build()
//                        val subtitleUri = listOf(MediaItem.Subtitle("${requireContext().cacheDir}/subtitles.vtt".toUri(), MimeTypes.TEXT_VTT, "en"))
                        val mediaItem = MediaItem.fromUri(url)
//                        setSubtitles(subtitleUri)
                        exoPlayer?.setMediaItem(mediaItem)
                        playerView.player = exoPlayer
                        exoPlayer?.prepare()
                        exoPlayer?.play()
                        requestAudioFocus()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        (activity as MainActivity).showDiaglo("Something went wrong $result")
                        println("there you go an error $result")
                    }
                }
            }





        }
         catch (e: Exception) {
            println("error found in this ${e.message}")
            (activity as MainActivity).showDiaglo("found an error here ${e.message}")
        }
    }



    private fun handleDoubleTap(event: MotionEvent?) {
        event?.let {
            // Get the x position of the double-tap event
            val xPos = event.x
            val videoWidth = playerView.width

            // Fast forward if double tap is on the right half of the screen
            if (xPos > videoWidth / 2) {
                fastForward()
            } else {
                rewind()
            }
        }
    }

    private fun fastForward() {
        exoPlayer?.let {
            val newPosition = it.currentPosition + fastForwardInterval
            it.seekTo(newPosition)
        }
    }

    private fun rewind() {
        exoPlayer?.let {
            val currentPosition = it.currentPosition
            val newPosition = currentPosition - rewindInterval
            it.seekTo(newPosition)
        }
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->

        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Audio focus gained, resume playback if it was paused
                if (exoPlayer?.playWhenReady == false) {
                    exoPlayer?.playWhenReady=true
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (exoPlayer?.playWhenReady == true) {
                    exoPlayer?.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Temporarily lost audio focus (e.g., incoming call), pause playback
                if (exoPlayer?.playWhenReady == true) {
                    exoPlayer?.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lost audio focus but can duck (e.g., lower volume)
                if (exoPlayer?.playWhenReady == true) {
                    exoPlayer?.volume = 0.1f // Lower volume when focus is lost transiently
                }
            }
        }
    }



    private fun requestAudioFocus() {

        audioManager = requireContext().getSystemService(AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()

            val focusRequestResult = audioManager.requestAudioFocus(audioFocusRequest!!)
            if (focusRequestResult == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                Log.e("AudioService", "Failed to gain audio focus")
            }
        } else {
            // For older versions, use the deprecated method
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                Log.e("AudioService", "Failed to gain audio focus")
            }
        }
    }


    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
                audioFocusRequest = null
                Log.d("AudioService", "Audio focus released for API >= 26")
            }
        }else {
            audioManager.abandonAudioFocus(audioFocusChangeListener)
            Log.d("AudioService", "Audio focus released for API < 26")
        }
    }








    private fun toggleFullScreen() {

        if (isFullScreen) {
            val videoPlayerHeight = resources.getDimensionPixelSize(R.dimen.video_player_height)
            binding.hideThese.visibility = View.VISIBLE
//            view?.findViewById<LinearLayout>(R.id.upper_controller_of_first)?.visibility= View.VISIBLE
            activity?.requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            view?.findViewById<TextView>(R.id.set_title_over_view)?.visibility= View.GONE
            binding.videoPlayerLocally.layoutParams.height = videoPlayerHeight

            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.fullscreen_24dp)
            view?.findViewById<ImageButton>(R.id.exofullscreen)?.setImageDrawable(drawable)
            showSystemUI()
            isFullScreen = false
        }else{
            binding.hideThese.visibility = View.GONE
            binding.videoPlayerLocally.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
//            view?.findViewById<LinearLayout>(R.id.upper_controller_of_first)?.visibility= View.VISIBLE
            view?.findViewById<TextView>(R.id.set_title_over_view)?.visibility= View.VISIBLE
            activity?.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.fullscreen_exit)
            view?.findViewById<ImageButton>(R.id.exofullscreen)?.setImageDrawable(drawable)
            hideSystemUI()
            isFullScreen= true
        }


    }





    private fun formatViews(views: Long): String {
        return when {
            views >= 1_000_000_000 -> "%.1fB".format(views / 1_000_000_000.0)
            views >= 1_000_000 -> "%.1fM".format(views / 1_000_000.0)
            views >= 1_000 -> "%.1fK".format(views / 1_000.0)
            else -> views.toString()
        }
    }
    fun formatDate(dateStr: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

            val zonedDateTime = ZonedDateTime.parse(dateStr, inputFormatter)

            val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

            return zonedDateTime.format(outputFormatter)
        }
        else{
            return dateStr
        }
    }






    private fun imageViewer(selectedID: String, title: String) {
        val thumbnailUrl = "https://img.youtube.com/vi/$selectedID/0.jpg"
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_with_image, null)
        val imageView: ImageView = dialogView.findViewById(R.id.dialog_image)

        Glide.with(this)
            .load(thumbnailUrl)
            .error(R.drawable.close)
            .centerCrop()
            .into(imageView)
        AlertDialog.Builder(context)
            .setTitle("Do you want to download it as video or audio?")
            .setView(dialogView)
            .setPositiveButton("Video") { _, _ ->
                (activity as MainActivity).downloadVideo(
                    selectedID,
                    title,
                    requireContext()
                )
            }
            .setNegativeButton("Music") { _, _ ->
                (activity as MainActivity).downloadMusic(
                    selectedID,
                    title,
                    requireContext()
                )
            }.show()
    }




    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (isInPictureInPictureMode){
            binding.hideThese.visibility = View.GONE
            binding.videoPlayerLocally.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            binding.videoPlayerLocally.hideController()
            view?.findViewById<TextView>(R.id.set_title_over_view)?.visibility= View.VISIBLE
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.fullscreen_exit)
            view?.findViewById<ImageButton>(R.id.exofullscreen)?.setImageDrawable(drawable)

            isFullScreen= true
        }else{
            binding.videoPlayerLocally.showController()
            toggleFullScreen()
        }
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


    private fun callPythonSearchSuggestion(inputText: String): List<Video>? {
        return try {

            val py = Python.getInstance()
            val mainFile = py.getModule("main")
            val getResultFromPython = mainFile["Searcher"]?.call(inputText).toString()
            val videoListType = object : TypeToken<List<Video>>() {}.type
            Gson().fromJson(getResultFromPython, videoListType)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    data class Video(
        val videoId: String,
        val title: String,
        val views: String,
        val dateOfVideo: String,
        val duration: String,
        val channelName: String,
        val channelThumbnailsUrl: String
    )



    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideBottomNav()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        releaseAudioFocus()
        binding.giveMeTitle.text= ""
        (activity as MainActivity).showBottomNav()
        _binding = null
    }




}

object GlobalVideoList {
    val listOfVideos = mutableListOf<ViewerFragment.Video>()
    val previousVideos = mutableListOf<ViewerFragment.Video>()
}

@file:Suppress("DEPRECATION")
package com.das.forui.ui.viewer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
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
import android.widget.TextView
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.das.forui.CustomTheme
import com.das.forui.services.AudioServiceFromUrl
import com.das.forui.DownloaderClass
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.databased.DatabaseFavorite
import com.das.forui.databinding.VideoViewerBinding
import com.das.forui.ui.viewer.GlobalVideoList.listOfVideosListData
import com.das.forui.ui.viewer.GlobalVideoList.previousVideosListData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.das.forui.MainActivity.Youtuber.pythonInstant
import com.das.forui.MainApplication
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_START
import com.das.forui.objectsAndData.VideosListData
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters
import kotlinx.coroutines.CoroutineScope


class ViewerFragment: Fragment() {
    private lateinit var duration: String
    private lateinit var videoURL: String
    private lateinit var videoTitle: String
    private lateinit var videoID: String
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
    private lateinit var channelName: String
    private lateinit var dateOfVideo: String
    private lateinit var viewNumber: String

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
        listOfVideosListData.clear()
        (activity as MainActivity).hideBottomNav()
        videoID = arguments?.getString("View_ID").toString()
        playerView = binding.videoPlayerLocally

        playVideo("https://www.youtube.com/watch?v=$videoID")


        val setTitle = view.findViewById<TextView>(R.id.set_title_over_view)


        videoID = arguments?.getString("View_ID").toString()
        videoTitle = arguments?.getString("View_Title").toString()
        videoURL = arguments?.getString("View_URL").toString()
        channelThumbnail = arguments?.getString("channel_Thumbnails").toString()
        viewNumber = arguments?.getString("View_Number").toString()
        dateOfVideo = arguments?.getString("dateOfVideo").toString()
        channelName = arguments?.getString("channelName").toString()
        duration = arguments?.getString("duration").toString()


        val videoPlayerHeight = resources.getDimensionPixelSize(R.dimen.video_player_height)
        binding.hideThese.visibility = View.VISIBLE

        @SuppressLint("SourceLockedOrientationActivity")
        (activity as MainActivity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
                    CustomTheme {
                        SuggestedVideos(
                            videoTitle
                        )
                    }
                }
            }
            Glide.with(requireContext())
                .load(channelThumbnail)
                .transform(CircleCrop())
                .into(binding.channelImageVideoView)

        } else {
            if (isAdded && _binding!= null) {
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
                                    CustomTheme {
                                        SuggestedVideos(
                                            videoTitle
                                        )
                                    }
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
            playThisOne(2)
        }
    }


    override fun onStart() {
        super.onStart()


        exoPlayer?.addListener(myExoPlayerListener())
        gestureDetector = GestureDetector(requireContext(), gestureDetector())

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
                    val playIntent = Intent(requireContext(), AudioServiceFromUrl::class.java).apply {
                        action = ACTION_START
                        putExtra("videoId", videoID)
                        putExtra("media_url", url)
                        putExtra("title", videoTitle)
                        putExtra("channelName", channelName)
                        putExtra("viewNumber",viewNumber)
                        putExtra("videoDate", dateOfVideo)
                        putExtra("duration", duration)
                    }
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
            (activity as MainActivity).downloadMusic(videoID, videoTitle, requireContext())
        }
        binding.downloadAsVideo.setOnClickListener {
            DownloaderClass(requireContext()).downloadVideo(url, videoTitle, "mp4")
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
                val videoChannelName = view?.findViewById<TextView>(R.id.give_me_view_channelName)?.text.toString()
                val videoViewCount = view?.findViewById<TextView>(R.id.give_me_view_number)?.text.toString()
                val videoDate = view?.findViewById<TextView>(R.id.give_me_view_date)?.text.toString()
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
            (activity as MainActivity).showDialogs(toastEr)
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





    private fun playThisOne(gotIndex: Int = 1, videosListDataDetails: VideosListData = listOfVideosListData[gotIndex]){
        exoPlayer?.let {
            it.stop()
            it.release()
        }
        val bundle = Bundle().apply {
            putString("View_ID", videosListDataDetails.videoId)
            putString("View_URL", "https://www.youtube.com/watch?v=${videosListDataDetails.videoId}")
            putString("View_Title", videosListDataDetails.title)
            putString("View_Number", videosListDataDetails.views)
            putString("dateOfVideo", videosListDataDetails.dateOfVideo)
            putString("channelName", videosListDataDetails.channelName)
            putString("duration", videosListDataDetails.duration)
            putString("channel_Thumbnails", videosListDataDetails.channelThumbnailsUrl)
        }
        previousVideosListData.add(videosListDataDetails)
        findNavController().run {
            popBackStack()
            navigate(R.id.nav_video_viewer, bundle)
        }

    }



    @Composable
    fun SuggestedVideos( titleVideo: String) {

        val searchResults = remember { mutableStateOf<List<VideosListData>>(emptyList()) }
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
//            AndroidView(
//                { context ->
//
//                    LayoutInflater.from(context)
//                        .inflate(android.R.layout.activity_list_item, null)
//                        .apply {                     // Nested Scroll Interop will be Enabled when                     // nested scroll is enabled for the root view                     ViewCompat. setNestedScrollingEnabled(this, true)                 }         }
//                        }
//                }
//            )

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
                                    title = searchItem.title,
                                    viewsNumber = searchItem.views,
                                    dateOfVideo = searchItem.dateOfVideo,
                                    channelName = searchItem.channelName,
                                    duration = searchItem.duration,
                                    videoThumbnailURL = searchItem.videoId,
                                    channelThumbnails = searchItem.channelThumbnailsUrl
                                    )
                                listOfVideosListData.add(searchItem)

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
        title: String,
        viewsNumber: String,
        dateOfVideo: String,
        channelName: String,
        duration: String,
        videoThumbnailURL: String,
        channelThumbnails: String
    ){
        val context= LocalContext.current


        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(1))
                .fillMaxWidth()
                .padding(bottom = 3.dp, top = 3.dp)
                .combinedClickable(
                    onClick = {
                        playThisOne(
                            videosListDataDetails = VideosListData(
                                videoId, title, viewsNumber, dateOfVideo,
                                duration, channelName, channelThumbnails
                            )
                        )
                    },
                    onLongClick = {
                        imageViewer(
                            VideosListData(
                            videoId, title, viewsNumber, dateOfVideo,
                            duration, channelName, channelThumbnails
                        ))
                    }
                )
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
                            .padding(end = 3.dp, bottom = 3.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xCC2C2B2B), RoundedCornerShape(5.dp))
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
                            text = title,
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
                            imageViewer(
                                VideosListData(
                                    videoId, title, viewsNumber, dateOfVideo,
                                    duration, channelName, channelThumbnails
                                )
                            )
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
            if (isAdded) {
                val trackSelector = DefaultTrackSelector(requireContext())
                val trackSelectionParameters = TrackSelectionParameters.Builder()
                    .setMaxVideoBitrate(100_000_000)
                    .build()
                trackSelector.setParameters(trackSelectionParameters)

                CoroutineScope(Dispatchers.IO).launch {
                    val mainFile = pythonInstant.getModule("main")
                    val variable = mainFile["get_video_url"]
                    val result = variable?.call("https://www.youtube.com/watch?v=$uri").toString()
                    if (result != "False") {
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
                            MainActivity().requestAudioFocusFromMain(requireContext(), exoPlayer)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            (activity as MainActivity).showDialogs("Something went wrong $result")
                            println("there you go an error $result")
                        }
                    }
                }
            }


        }
         catch (e: Exception) {
            println("error found in this ${e.message}")
            (activity as MainActivity).showDialogs("found an error here ${e.message}")
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





    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            audioFocusRequest?.let {
//                audioManager.abandonAudioFocusRequest(it)
                Log.d("AudioService", "Audio focus released for API >= 26")
//            } ?: run {
                Log.w("AudioService", "audioFocusRequest is null, cannot release focus")
//            }
        } else {
//            audioManager.abandonAudioFocus(audioFocusChangeListener)
            Log.d("AudioService", "Audio focus released for API < 26")
        }
    }








    @SuppressLint("SourceLockedOrientationActivity")
    private fun toggleFullScreen() {

        if (isFullScreen) {
            val videoPlayerHeight = resources.getDimensionPixelSize(R.dimen.video_player_height)
            binding.hideThese.visibility = View.VISIBLE
            (activity as MainActivity).requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
    private fun formatDate(dateStr: String): String {
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






    private fun imageViewer(selectedItem: VideosListData) {
        val thumbnailUrl = "https://img.youtube.com/vi/${selectedItem.videoId}/0.jpg"
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_with_image, null)
        val imageView: ImageView = dialogView.findViewById(R.id.dialog_image)

        Glide.with(this)
            .load(thumbnailUrl)
            .error(R.drawable.close)
            .centerCrop()
            .into(imageView)

        AlertDialog.Builder(context)
            .setTitle("Do you want to download it as video or audio? or play it in background")
            .setView(dialogView)
            .setPositiveButton("Video") { _, _ ->
                (activity as MainActivity).downloadVideo(
                    selectedItem.videoId,
                    selectedItem.title,
                    requireContext()
                )
            }
            .setNegativeButton("Music") { _, _ ->
                (activity as MainActivity).downloadMusic(
                    selectedItem.videoId,
                    selectedItem.title,
                    requireContext()
                )
            }
            .setNeutralButton(
                "Background"
            ) { _, _ ->
                MainApplication().getListItemsStreamUrls(
                    selectedItem,
                    onSuccess = { result ->
                        val playIntent = Intent(requireContext(), AudioServiceFromUrl::class.java).apply {
                            action = ACTION_START
                            putExtra("videoId", selectedItem.videoId)
                            putExtra("media_url", result.audioUrl)
                            putExtra("title", selectedItem.title)
                            putExtra("channelName", selectedItem.channelName)
                            putExtra("viewNumber",selectedItem.views)
                            putExtra("videoDate", selectedItem.dateOfVideo)
                            putExtra("duration", selectedItem.duration)
                        }
                        activity?.startService(playIntent)
                    },
                    onFailure = { errorMessage ->
                        // Handle the error (e.g., show a dialog with the error message)
                        println("Error: $errorMessage")
                    }
                )

            }
            .show()
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




    private fun myExoPlayerListener():Player.Listener {

        return object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                if (state == Player.STATE_ENDED) {
                    playThisOne(1)
//                        playVideo("https://rr1---sn-uigxx03-ajtl.googlevideo.com/videoplayback?expire=1733809712&ei=0IFXZ4yDM8nLmLAP0KuXkQs&ip=2a04%3A4a43%3A977f%3Af392%3A488%3Af57f%3A2e91%3Af970&id=o-AP1jvq5SB4PyoIxftzFcyDhnOoJYtFkz6gwzvCV4dZKr&itag=18&source=youtube&requiressl=yes&xpc=EgVo2aDSNQ%3D%3D&met=1733788112%2C&mh=SH&mm=31%2C29&mn=sn-uigxx03-ajtl%2Csn-aigzrn7e&ms=au%2Crdu&mv=m&mvi=1&pl=57&rms=au%2Cau&pcm2=yes&initcwndbps=1001250&bui=AQn3pFQRRCfZ-thiT9XXa8gdCyODGraPQCWec-TG-n3No2CqTbAVSg6FUqsvPEtkTSDOLoz-FGRKjDsp&vprv=1&mime=video%2Fmp4&rqh=1&cnr=14&ratebypass=yes&dur=903.093&lmt=1725726923730991&mt=1733787646&fvip=4&fexp=51326932%2C51335594%2C51347747&c=ANDROID_VR&txp=5309224&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cxpc%2Cpcm2%2Cbui%2Cvprv%2Cmime%2Crqh%2Ccnr%2Cratebypass%2Cdur%2Clmt&sig=AJfQdSswRQIgZYrNHQWM0OMLTt-ZuAruMoa1rHNpXGmSZSSp2Y3b8d4CIQDruSFUA4ppu_RurRrSokhMKFZg7kHtWm2rwpxMeRGRqQ%3D%3D&lsparams=met%2Cmh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Crms%2Cinitcwndbps&lsig=AGluJ3MwRQIgd2lflxQvzlD9TkorrTdzHil3a7X_mA7HUlg9HSrekIQCIQCPEHdGpJ2YfrK_nYrSqnNvVpBBgG2SAOYZPDa6DIkzzA%3D%3D")
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
                    MainActivity().requestAudioFocusFromMain(requireContext(), exoPlayer)
                } else {
                    isPlaying = false
                    println("service pausing")
                    releaseAudioFocus()
                }
            }

        }
    }
    private fun gestureDetector(): GestureDetector.SimpleOnGestureListener {

        return object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                handleDoubleTap(e)
                return super.onDoubleTap(e)
            }
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


    private fun callPythonSearchSuggestion(inputText: String): List<VideosListData>? {
        return try {

            val mainFile = pythonInstant.getModule("main")
            val getResultFromPython = mainFile["Searcher"]?.call(inputText).toString()
            val videosListDataListType = object : TypeToken<List<VideosListData>>() {}.type
            Gson().fromJson(getResultFromPython, videosListDataListType)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }



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
    val listOfVideosListData = mutableListOf<VideosListData>()
    val previousVideosListData = mutableListOf<VideosListData>()
}

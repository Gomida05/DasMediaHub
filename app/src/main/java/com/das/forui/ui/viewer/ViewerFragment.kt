package com.das.forui.ui.viewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.ITALIC
import android.os.Bundle
import android.os.PowerManager
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.More
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.das.forui.services.AudioServiceFromUrl
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.ui.viewer.GlobalVideoList.listOfVideosListData
import com.das.forui.ui.viewer.GlobalVideoList.previousVideosListData
import com.das.forui.MainApplication
import com.das.forui.databased.DatabaseFavorite
import com.das.forui.databased.WatchHistory
import com.das.forui.mediacontroller.VideoPlayerControllers.PlayerControls
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_START
import com.das.forui.objectsAndData.ForUIKeyWords.NEW_INTENT_FOR_VIEWER
import com.das.forui.objectsAndData.ForUIDataClass.VideoDetails
import com.das.forui.objectsAndData.ForUIDataClass.VideosListData
import com.das.forui.ui.viewer.GlobalVideoList.bundles
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun VideoPlayerScreen(
    navController: NavController,
    arguments: Bundle?
) {


    var showAlertDialog by remember { mutableStateOf(false) }

    listOfVideosListData.clear()
    val viewModel: ViewerViewModel = viewModel()
    val suggestionViewModel: ViewerViewModel = viewModel()

    val videoID = arguments?.getString("View_ID").toString()

    val mContext = LocalContext.current
    var videoTitle by remember {
        mutableStateOf(
            arguments?.getString("View_Title")
        )
    }

    var videoDuration by remember {
        mutableStateOf(
            arguments?.getString("duration")
        )
    }

    var videoViews by remember {
        mutableStateOf(
            arguments?.getString("View_Number")
        )
    }

    var videoDate by remember {
        mutableStateOf(
            arguments?.getString("dateOfVideo")
        )
    }
    var videoChannelName by remember {
        mutableStateOf(
            arguments?.getString("channelName")
        )
    }

    var videoChannelThumbnails by remember {
        mutableStateOf(
            arguments?.getString("channel_Thumbnails")
        )
    }


    var videoUrl by remember { mutableStateOf("") }


    val isLoading by suggestionViewModel.isLoadingVideos
    val searchResults by suggestionViewModel.searchResults

    LaunchedEffect(videoTitle) {
        if (!videoTitle.isNullOrEmpty()) {
            suggestionViewModel.fetchSuggestions(videoTitle!!)

        }
    }

    Scaffold { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopCenter)
                    .background(Color.Black)
                    .height(220.dp)
                    .fillMaxWidth(),
            ) {
                ExoPlayerUI(
                    navController,
                    currentId = videoID,
                    viewModel = viewModel,
                    done = {videoUrl= it}
                )

            }

            LazyColumn {

                item(videoID) {
                    VideoDetailsComposable(
                        mContext = mContext,
                        videoId = videoID,
                        channelThumbnailURL = videoChannelThumbnails ?: "none is here",
                        duration = videoDuration ?: "0:00",
                        viewModel = viewModel,
                        clickForMore = {
                            showAlertDialog = true
                        },
                        downloadAsVideo = {
                            Toast.makeText(mContext, "Downloading has started", Toast.LENGTH_SHORT)
                                .show()
                            MainActivity().startDownloadingVideo(mContext, videoID, it)
//                            DownloaderClass(mContext).downloadVideo(videoUrl, it, "mp4")
                        },
                        downloadAsMusic = {
                            Toast.makeText(mContext, "Downloading has started", Toast.LENGTH_SHORT)
                                .show()
                            MainActivity().startDownloadingAudio(mContext, videoID, it)

                        },
                        finished = {
                            videoTitle = it.title
                            videoViews = it.viewNumber
                            videoDate = it.date
                            videoChannelName = it.channelName
                        }
                    )

                }

                if (isLoading) {
                    item {
                        SkeletonSuggestionLoadingLayout()
                    }
                } else {
                    if (searchResults.isEmpty()) {
                        item {

                            Text(
                                text = "No results found",
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(searchResults) { searchItem ->
                            if (searchItem.videoId == videoID) {
                                videoChannelThumbnails = searchItem.channelThumbnailsUrl
                                videoDuration = searchItem.duration
                            }
                            CategoryItems(
                                navController,
                                searchItem
                            )
                            listOfVideosListData.add(searchItem)

                        }

                    }

                }
            }
        }

    }

    if (showAlertDialog){
        AskToPlay(
            mContext,
            videoUrl,
            VideosListData(
                videoID,
                videoTitle.toString(),
                videoViews.toString(),
                videoDate.toString(),
                videoDuration.toString(),
                videoChannelName.toString(),
                videoChannelThumbnails.toString()
            ),
            onDismissRequest = {
                showAlertDialog = false
            }
        )
    }
}





@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun ExoPlayerUI(
    navController: NavController,
    currentId: String,
    viewModel: ViewerViewModel,
    done: (url: String) ->Unit,
) {
    val mContext = LocalContext.current

    val videoUrl by viewModel.videoUrl

    val isLoading by viewModel.isLoading

    val isThereError by viewModel.error

    val powerManager = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    @Suppress("DEPRECATION")
    val wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp:VideoPlayer")

    // Acquire the wake lock when the video is playing



    LaunchedEffect(currentId) {
        viewModel.loadVideoUrl(currentId)
    }


    if (isThereError.isNotEmpty()){
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            Image(
                imageVector = Icons.Default.Error,
                "",
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        return
    }

    if (isLoading){
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }


    if (videoUrl.isNotEmpty() && !isLoading) {

        val mExoPlayer = remember(mContext) {
            done(videoUrl)
            ExoPlayer.Builder(mContext).build().apply {
                hasNextMediaItem()
                setMediaItem(MediaItem.fromUri(videoUrl))
                prepare()
                playWhenReady = true

                MainActivity().requestAudioFocusFromMain(mContext, this)
                addListener(MyExoPlayerListener(navController))
            }
        }
        var controlsVisible by remember { mutableStateOf(true) }
        val coroutineScope = rememberCoroutineScope()

        fun startAutoHideTimer() {
            coroutineScope.launch {
                delay(4000) // 3 seconds
                controlsVisible = false
            }
        }


        LaunchedEffect(Unit) {
            startAutoHideTimer()
        }


        // Auto-hide after 3 seconds of visibility
        DisposableEffect(mExoPlayer) {
            onDispose {
                mExoPlayer.release()
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
        }


        val presentationState = rememberPresentationState(mExoPlayer)

        LaunchedEffect(mExoPlayer.isPlaying) {
            if (mExoPlayer.isPlaying) {
                val videoDurationMs = mExoPlayer.duration / 1000
                if (!wakeLock.isHeld) {
                    wakeLock.acquire(videoDurationMs)
                }
            } else {
                // Set default timeout (e.g., 10 minutes) when video is paused or stopped
                if (!wakeLock.isHeld) {
                    wakeLock.acquire(2 * 60 * 1000L) // 10 minutes
                }
            }
        }


        val scaledModifier = Modifier
            .fillMaxSize()
            .resizeWithContentScale(ContentScale.Fit, presentationState.videoSizeDp)
            .pointerInput(Unit) {
                detectTapGestures {
                    if (controlsVisible) {
                        controlsVisible = false // hide immediately
                    } else {
                        controlsVisible = true
                        startAutoHideTimer()
                    }
                }
            }

        PlayerSurface(
            mExoPlayer,
            surfaceType = SURFACE_TYPE_SURFACE_VIEW,
            modifier = scaledModifier
        )

        if (presentationState.coverSurface) {
            // Cover the surface that is being prepared with a shutter
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black))
        }

        PlayerControls(
            mExoPlayer = mExoPlayer,
            isVisible ={controlsVisible},
            navigateUp = {
                navController.navigateUp()
            }
        )



    }


}








@Composable
fun VideoDetailsComposable(
    mContext: Context,
    videoId: String,
    channelThumbnailURL: String,
    duration: String,
    viewModel: ViewerViewModel,
    clickForMore: () -> Unit,
    downloadAsVideo: (videoTitle: String) -> Unit,
    downloadAsMusic: (title: String) -> Unit,
    finished: (title: VideoDetails) ->Unit
) {

    var showDescriptionDialog by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoadings

    val dbForFav = DatabaseFavorite(mContext)

    val videoDetails by viewModel.videoDetails.observeAsState()
    var isSaved by remember {
        mutableStateOf(
            dbForFav.isWatchUrlExist(videoId)
        )
    }
    val colorForFavIcon = if (isSystemInDarkTheme()) Color.Unspecified else Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {


        LaunchedEffect(videoId) {

            viewModel.fetchVideoDetails(videoId)
        }



        if (isLoading || videoDetails == null) {
            SkeletonLoadingLayout()

        } else {

            val title = videoDetails?.title.toString()
            finished(videoDetails!!)

            WatchHistory(mContext).insertNewVideo(
                videoId,
                title,
                videoDetails?.date.toString(),
                videoDetails?.viewNumber.toString(),
                videoDetails?.channelName.toString(),
                duration,
                channelThumbnailURL
            )

            Text(
                text = title,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = {
                        showDescriptionDialog = true
                    })
            )
//                Spacer(modifier = Modifier.height(5.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                AsyncImage(
                    model = ImageRequest.Builder(mContext)
                        .data(channelThumbnailURL)
                        .crossfade(true)
                        .error(R.mipmap.under_development)
                        .build(),
                    contentDescription = "Category Image",
                    modifier = Modifier
                        .size(34.dp, 34.dp)
                        .clip(RoundedCornerShape(50)),
                    alignment = Alignment.CenterStart,
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = videoDetails?.channelName.toString(),
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .width(142.dp)
                        .align(Alignment.CenterVertically)
                        .padding(bottom = 3.dp)
                )

                Text(
                    text = videoDetails?.viewNumber.toString(),
                    maxLines = 1,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center)
                        .width(52.dp)
                        .padding(bottom = 3.dp)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = videoDetails?.date.toString(),
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "https://www.youtube.com/watch?v=${videoId}&feature=shared"
                            )
                        }

                        val chooser = Intent.createChooser(shareIntent, "Share via")
                        mContext.startActivity(chooser)
                    },
                    shape = RoundedCornerShape(25)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.Share),
                        ""
                    )
                }
                Button(
                    onClick = {
                        if (isSaved) {
                            dbForFav.deleteWatchUrl(videoId)
                            isSaved = false
                        } else {
                            dbForFav.insertData(
                                videoId,
                                title,
                                videoDetails?.date.toString(),
                                videoDetails?.viewNumber.toString(),
                                videoDetails?.channelName.toString(),
                                duration,
                                channelThumbnailURL
                            )
                            isSaved = true
                        }
                    },
                    shape = RoundedCornerShape(25)
                ) {
                    Icon(
                        painter = rememberVectorPainter(
                            if (isSaved) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            }
                        ),
                        contentDescription = if (isSaved) "Saved" else "Not Saved",
                        tint = if (isSaved) Color.Red else colorForFavIcon
                    )
                }
                Button(
                    onClick = {
                        downloadAsMusic(title)
                    },
                    shape = RoundedCornerShape(25)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.MusicNote),
                        ""
                    )
                }

                Button(
                    onClick = {
                        downloadAsVideo(
                            title
                        )
                    },
                    shape = RoundedCornerShape(25)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.SmartDisplay),
                        ""
                    )
                }

                Button(
                    onClick = {
                        clickForMore()
                    },
                    shape = RoundedCornerShape(25)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.AutoMirrored.Default.More),
                        ""
                    )
                }
            }
        }
    }

    if (showDescriptionDialog) {
        ShowDescriptionDialog(
            videoDetails?.description!!
        ) {
            showDescriptionDialog = false
        }
    }

}



@Composable
fun CategoryItems(
    navController: NavController,
    searchItem: VideosListData
) {
    val context = LocalContext.current
    val videoId = searchItem.videoId
    val title = searchItem.title
    val viewsNumber = searchItem.views
    val dateOfVideo = searchItem.dateOfVideo
    val channelName = searchItem.channelName
    val duration = searchItem.duration
    val videoThumbnailURL = searchItem.videoId
    val channelThumbnails = searchItem.channelThumbnailsUrl

    var showDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(1))
            .fillMaxWidth()
            .padding(bottom = 3.dp, top = 3.dp)
            .combinedClickable(
                onClick = {
                    playThisOne(
                        navController= navController,
                        videosListDataDetails = VideosListData(
                            videoId, title, viewsNumber, dateOfVideo,
                            duration, channelName, channelThumbnails
                        )
                    )
                },
                onLongClick = {
                    showDialog = true
                }
            )
    ) {
        Column(
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
                        showInfoDialog = true
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
                        showDialog = true
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

    if (showDialog || showInfoDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
    }

    if (showInfoDialog){
        AlertDialogForUser{
            showInfoDialog = false
        }

    }

    if (showDialog) {
        ShowAlertDialog(
            mContext = context,
            selectedItem = VideosListData(
                videoId, title, viewsNumber, dateOfVideo,
                duration, channelName, channelThumbnails
            ),
            onDismissRequest = { showDialog = false }
        )
    }
}


@SuppressLint("UnsafeOptInUsageError")
@Composable
fun FullScreenExoPlayer(navController: NavController, mExoPlayer: ExoPlayer){

}



private fun playThisOne(
    navController: NavController,
    gotIndex: Int = 1,
    videosListDataDetails: VideosListData = listOfVideosListData[gotIndex]
) {
    val bundle = Bundle().apply {
        putString("View_ID", videosListDataDetails.videoId)
        putString(
            "View_URL",
            "https://www.youtube.com/watch?v=${videosListDataDetails.videoId}"
        )
        putString("View_Title", videosListDataDetails.title)
        putString("View_Number", videosListDataDetails.views)
        putString("dateOfVideo", videosListDataDetails.dateOfVideo)
        putString("channelName", videosListDataDetails.channelName)
        putString("duration", videosListDataDetails.duration)
        putString("channel_Thumbnails", videosListDataDetails.channelThumbnailsUrl)
    }
    previousVideosListData.add(videosListDataDetails)

    bundles.putBundle(NEW_INTENT_FOR_VIEWER, bundle)
    navController.run {
        popBackStack()
        navigate("video viewer")
    }


}


@Composable
private fun ShowDescriptionDialog(
    text: String,
    onDismissRequest: () -> Unit
) {

    val urlPattern = """https?://\S+""".toRegex()

    val matches = urlPattern.findAll(text)

    val spannable = SpannableString(text)

    matches.forEach { match ->
        val url = match.value
        val startIndex = match.range.first
        val endIndex = match.range.last + 1
        spannable.setSpan(URLSpan(url), startIndex, endIndex, 0)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                "Descriptions"
            )
        },
        text = {
            Text(
                text = spannable.toAnnotatedString(),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(10.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Close")
            }
        }


    )
}

@Composable
private fun ShowAlertDialog(
    mContext: Context,
    selectedItem: VideosListData,
    onDismissRequest: () ->Unit
){
    val thumbnailUrl = "https://img.youtube.com/vi/${selectedItem.videoId}/0.jpg"

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(13.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Do you want to download it as video or audio? or play it in background?",
                    modifier = Modifier.padding(8.dp),
                )
                AsyncImage(
                    model = ImageRequest.Builder(mContext)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Category Image",
                    modifier = Modifier
                        .height(190.dp)
                        .clip(RoundedCornerShape(4)),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = {
                            MainApplication().getListItemsStreamUrls(
                                selectedItem,
                                onSuccess = { result ->
                                    val playIntent = Intent(mContext, AudioServiceFromUrl::class.java).apply {
                                            action = ACTION_START
                                            putExtra("videoId", selectedItem.videoId)
                                            putExtra("media_url", result.audioUrl)
                                            putExtra("title", selectedItem.title)
                                            putExtra("channelName", selectedItem.channelName)
                                            putExtra("viewNumber", selectedItem.views)
                                            putExtra("videoDate", selectedItem.dateOfVideo)
                                            putExtra("duration", selectedItem.duration)
                                        }
                                    mContext.startService(playIntent)
                                },
                                onFailure = { errorMessage ->
                                    // Handle the error (e.g., show a dialog with the error message)
                                    println("Error: $errorMessage")
                                }
                            )
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Text("Background")
                    }
                    TextButton(
                        onClick = {
                            MainActivity().startDownloadingAudio(
                                mContext,
                                selectedItem.videoId,
                                selectedItem.title
                            )
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Text("Music")
                    }
                    TextButton(
                        onClick = {
                            MainActivity().startDownloadingVideo(
                                mContext,
                                selectedItem.videoId,
                                selectedItem.title
                            )
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Text("Video")
                    }
                }
            }
        }
    }

}




private class MyExoPlayerListener(
    private val navController: NavController
) : Player.Listener {
    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        if (state == Player.STATE_ENDED) {
            playThisOne(navController,1)

        }
    }
}




@Composable
fun SkeletonSuggestionLoadingLayout() {
    // Placeholder UI for loading state
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(495.dp)
            .padding(8.dp)
    ) {
        // Placeholder for each video item (image + text)
        repeat(5) { // Repeat for a few video items to show skeletons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(bottom = 12.dp)
                    .background(Color.Gray.copy(alpha = 0.2f)) // Placeholder background
            ) {
                // Video thumbnail placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .background(Color.Gray.copy(alpha = 0.2f))
                )

                // Duration placeholder (bottom right)
                Text(
                    text = "",
                    maxLines = 1,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(end = 3.dp, bottom = 3.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                        .height(20.dp)
                        .width(50.dp)
                )
                // Title placeholder
                Text(
                    text = "",
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                // Channel name, views, and date placeholders
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(12.dp)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(12.dp)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}


@Composable
fun SkeletonLoadingLayout() {
        // This can be your custom skeleton loader UI
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Thumbnail and other details placeholders
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .width(142.dp)
                        .height(16.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(16.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .width(62.dp)
                        .height(16.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons placeholders
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }


@Composable
private fun AlertDialogForUser(
    onDismissRequest: () ->Unit
){
    AlertDialog(
        onDismissRequest= onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.mipmap.under_development),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text ="This feature is currently under development!!!",
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Text(
                "Thank you!😊",
                fontSize = 18.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                ) {
                Text("Okay")
            }
        }
    )
}


@Composable
private fun AskToPlay(
        mContext: Context,
        url: String,
        video: VideosListData,
        onDismissRequest: () -> Unit
) {
    val playIntent = Intent(mContext, AudioServiceFromUrl::class.java).apply {
            action = ACTION_START
            putExtra("videoId", video.videoId)
            putExtra("media_url", url)
            putExtra("title", video.title)
            putExtra("channelName", video.channelName)
            putExtra("viewNumber", video.views)
            putExtra("videoDate", video.dateOfVideo)
            putExtra("duration", video.duration)
        }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Do you want to play it in the background?")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    mContext.startService(playIntent)
                    onDismissRequest()
                }
            ) {
                Text("yes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("no")
            }
        }
    )

}







fun SpannableString.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        append(this@toAnnotatedString.toString())
        getSpans(0, length, Any::class.java).forEach { span ->
            val start = getSpanStart(span)
            val end = getSpanEnd(span)
            when (span) {
                is ForegroundColorSpan -> addStyle(
                    style = SpanStyle(color = Color(span.foregroundColor)),
                    start = start,
                    end = end
                )
                is BackgroundColorSpan -> addStyle(
                    style = SpanStyle(background = Color(span.backgroundColor)),
                    start = start,
                    end = end
                )
                is StyleSpan -> when (span.style) {
                    BOLD -> addStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold),
                        start = start,
                        end = end
                    )
                    ITALIC -> addStyle(
                        style = SpanStyle(fontStyle = FontStyle.Italic),
                        start = start,
                        end = end
                    )
                    // Add more style span cases as needed
                }
                is URLSpan -> {
                    addStyle(
                        style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                        start = start,
                        end = end
                    )
                    addStringAnnotation(
                        tag = "link",
                        annotation = span.url,
                        start = start,
                        end = end
                    )
                }
                // Add more span cases as needed
            }
        }
    }
}

object GlobalVideoList {
    val listOfVideosListData = mutableListOf<VideosListData>()
    val previousVideosListData = mutableListOf<VideosListData>()
    val bundles = Bundle()
}

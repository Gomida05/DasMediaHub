package com.das.forui.ui.viewer

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.URLSpan
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.More
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.das.forui.services.AudioServiceFromUrl
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.data.constants.GlobalVideoList.listOfVideosListData
import com.das.forui.data.constants.GlobalVideoList.previousVideosListData
import com.das.forui.NavScreens
import com.das.forui.WakeLockHelper
import com.das.forui.data.YouTuber.loadStreamUrl
import com.das.forui.data.databased.DatabaseFavorite
import com.das.forui.data.databased.WatchHistory
import com.das.forui.data.constants.Action.ACTION_START
import com.das.forui.data.constants.Intents.NEW_INTENT_FOR_VIEWER
import com.das.forui.data.model.VideoDetails
import com.das.forui.data.model.VideosListData
import com.das.forui.data.constants.GlobalVideoList.bundles
import com.das.forui.findActivity
import com.das.forui.ui.viewer.CustomMethods.SkeletonSuggestionLoadingLayout
import com.das.forui.ui.viewer.CustomMethods.SkeletonLoadingLayout
import com.das.forui.ui.viewer.CustomMethods.toAnnotatedString


@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    navController: NavController,
    arguments: Bundle?
) {

    var isInFullScreen by remember { mutableStateOf(false) }
    var showAlertDialog by remember { mutableStateOf(false) }

    var shouldEnterPipMode by remember { mutableStateOf(false) }

    val viewModel: ViewerViewModel = viewModel()


    val videoID = arguments?.getString("View_ID").toString()

    val mContext = LocalContext.current

    val currentShouldEnterPipMode by rememberUpdatedState(newValue = shouldEnterPipMode)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    ) {
        val context = LocalContext.current
        DisposableEffect(context) {
            val onUserLeaveBehavior = Runnable {
                    context.findActivity()
                        .enterPictureInPictureMode(PictureInPictureParams.Builder().build())

            }
            context.findActivity().addOnUserLeaveHintListener(
                onUserLeaveBehavior
            )
            onDispose {
                context.findActivity().removeOnUserLeaveHintListener(
                    onUserLeaveBehavior
                )
            }
        }
    } else {
        Log.i("PiP info", "API does not support PiP")
    }


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


    val isLoadingVideos by viewModel.isLoadingVideos
    val videosListResult by viewModel.searchResults

    val videoUrl by viewModel.videoUrl

    val isLoading by viewModel.isLoading

    val isThereError by viewModel.error

    val activity = LocalActivity.current
    LaunchedEffect(videoID) {
        viewModel.loadVideoUrl(videoID)
    }


    LaunchedEffect(videoTitle) {
        if (!videoTitle.isNullOrEmpty()) {
            viewModel.fetchSuggestions(videoTitle!!)

        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddings->

        Column(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize()
        ) {
            if (videoUrl.isNotEmpty() && !isLoading) {


                val mExoPlayer = remember(mContext) {

                    ExoPlayer.Builder(mContext)
                        .build()
                        .apply {
                            setMediaItem(MediaItem.fromUri(videoUrl.toUri()))
                            prepare()
                            play()
                            MainActivity().requestAudioFocusFromMain(mContext, this)
                            addListener(MyExoPlayerListener(navController))
                        }
                }


                DisposableEffect(mExoPlayer) {

                    WakeLockHelper.acquireWakeLock(mContext.applicationContext)
                    onDispose {
                        WakeLockHelper.releaseWakeLock()
                        mExoPlayer.release()
                        setFullscreen(activity, false)
                    }
                }

                mExoPlayer.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        shouldEnterPipMode = isPlaying
                        if (isPlaying) {
                            WakeLockHelper.acquireWakeLock(mContext.applicationContext)
                        } else {
                            WakeLockHelper.releaseWakeLock()
                        }
                    }
                })

                val playerModifier = if (isInFullScreen) {
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                } else {
                    Modifier
                        .height(220.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                }




                AndroidView(
                    modifier = playerModifier,
                    factory = { context ->
                        val view = LayoutInflater.from(context)
                            .inflate(R.layout.video_player_ui, null, false) as PlayerView

                        view.apply {
                            player = mExoPlayer
                            useController = true
                            keepScreenOn = true
                            setFullscreenButtonState(isInFullScreen)
                            setFullscreenButtonClickListener { isFullscreen ->
                                isInFullScreen = isFullscreen
                            }
                        }
                        view
                    },
                    update = { playerView ->
                        playerView.player = mExoPlayer
                    }
                )
            }
            else if (isLoading){
                Box(
                    modifier = Modifier
                        .height(220.dp)
                        .fillMaxWidth()
                        .background(Color.Black)
                ){
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            else if (isThereError.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .height(220.dp)
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    Image(
                        imageVector = Icons.Default.Error,
                        "",
                        modifier = Modifier
                            .fillMaxSize()
                    )

                }

            }
            if (!isInFullScreen){
                LaunchedEffect(videoID) {
                    viewModel.fetchVideoDetails(videoID)
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

                    if (isLoadingVideos) {
                        item {
                            SkeletonSuggestionLoadingLayout()
                        }
                    } else {
                        if (videosListResult.isEmpty()) {
                            item {

                                Text(
                                    text = "No results found",
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(videosListResult) { searchItem ->
                                if (searchItem.videoId == videoID) {
                                    videoChannelThumbnails = searchItem.channelThumbnailsUrl
                                    videoDuration = searchItem.duration
                                }
                                VideoLists(
                                    navController,
                                    searchItem,
                                )
                                listOfVideosListData.add(searchItem)

                            }

                        }

                    }
                }
            }
        }
    }


    LaunchedEffect(isInFullScreen) {

        activity?.let {

            setFullscreen(it, isInFullScreen)

            // Optional: lock orientation when fullscreen
            it.requestedOrientation = if (isInFullScreen) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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





fun setFullscreen(activity: Activity?, fullscreen: Boolean) {

    activity?.let {
        WindowCompat.setDecorFitsSystemWindows(it.window, !fullscreen)
        val controller = WindowInsetsControllerCompat(it.window, it.window.decorView)

        if (fullscreen) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
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

    var showDescriptionDialog by rememberSaveable { mutableStateOf(false) }
    var comingSoonDialog by rememberSaveable { mutableStateOf(false) }
    val isLoading by viewModel.isLoadings

    val dbForFav = DatabaseFavorite(mContext)

    val videoDetails by viewModel.videoDetails
    var isSaved by rememberSaveable {
        mutableStateOf(
            dbForFav.isWatchUrlExist(videoId)
        )
    }
//    val colorForFavIcon = if (isSystemInDarkTheme()) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {




        if (isLoading || videoDetails == null) {
            SkeletonLoadingLayout()

        } else {

            val title = videoDetails?.title.toString()
            finished(videoDetails!!)

            if (channelThumbnailURL != "none is here"){
                WatchHistory(mContext).insertNewVideo(
                    videoId,
                    title,
                    videoDetails?.date.toString(),
                    videoDetails?.viewNumber.toString(),
                    videoDetails?.channelName.toString(),
                    duration,
                    channelThumbnailURL
                )
            }

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
                        .clip(RoundedCornerShape(50))
                        .combinedClickable(
                            onClick = {
                                comingSoonDialog = true
                            }
                        ),
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
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "https://youtu.be/${videoId}?feature=shared"
                            )
//                                "https://www.youtube.com/watch?v=${videoId}&feature=shared"
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
                OutlinedButton(
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
                        tint = if (isSaved) Color.Red else MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedButton(
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

                OutlinedButton(
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

                OutlinedButton(
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

    if (comingSoonDialog){

        ComingSoonAlertDialog {
            comingSoonDialog = false
        }
    }

}



@Composable
private fun VideoLists(
    navController: NavController,
    searchItem: VideosListData,
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
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
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
            mContext = context.applicationContext,
            selectedItem = VideosListData(
                videoId, title, viewsNumber, dateOfVideo,
                duration, channelName, channelThumbnails
            ),
            onDismissRequest = { showDialog = false }
        )
    }
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
        navigate(NavScreens.VideoViewer.route)
    }


}


@Composable
private fun ComingSoonAlertDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest,
        icon = {
            Image(
                imageVector = Icons.Default.Info,
                ""
            )
        },
        title = {
            Text(
                "Sorry this feature is still underdevelopment!"
            )
        },

        text = {
            Text(
                "Thanks for your understanding"
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    "Okay"
                )
            }
        }
    )
}

@Composable
private fun ShowDescriptionDialog(text: String, onDismissRequest: () -> Unit) {

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
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .heightIn(max = 250.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp)
                ) {
                    Text(
                        text = spannable.toAnnotatedString(),
                        style = MaterialTheme.typography.bodySmall
                            .copy(textAlign = TextAlign.Start, fontSize = 14.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
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
    onDismissRequest: () -> Unit
) {
    val thumbnailUrl = "https://img.youtube.com/vi/${selectedItem.videoId}/0.jpg"


    var shouldLoad by remember { mutableStateOf(false) }

    if (shouldLoad) {
        LaunchedEffect(Unit) {
            loadStreamUrl(
                selectedItem,
                onSuccess = {
                    val playIntent = Intent(mContext, AudioServiceFromUrl::class.java).apply {
                        action = ACTION_START
                        putExtra("videoId", selectedItem.videoId)
                        putExtra("media_url", it.audioUrl)
                        putExtra("title", selectedItem.title)
                        putExtra("channelName", selectedItem.channelName)
                        putExtra("viewNumber", selectedItem.views)
                        putExtra("videoDate", selectedItem.dateOfVideo)
                        putExtra("duration", selectedItem.duration)
                    }
                    mContext.startService(playIntent)
                },
                onFailure = {
                    println("Error: $it")
                }
            )
            shouldLoad = false
        }
    }

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
                            shouldLoad = true
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








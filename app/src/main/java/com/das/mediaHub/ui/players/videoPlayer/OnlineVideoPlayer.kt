package com.das.mediaHub.ui.players.videoPlayer

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.das.mediaHub.services.AudioServiceFromUrl
import com.das.mediaHub.MainActivity
import com.das.mediaHub.R
import com.das.mediaHub.data.constants.GlobalVideoList.listOfVideosListData
import com.das.mediaHub.NavScreens
import com.das.mediaHub.OnLaunchComponents.openCustomTab
import com.das.mediaHub.OnLaunchComponents.playAudioFromUrl
import com.das.mediaHub.PIP.rememberIsInPipMode
import com.das.mediaHub.PIP.shouldEnterPipMode
import com.das.mediaHub.WakeLockHelper.acquireWakeLock
import com.das.mediaHub.WakeLockHelper.releaseWakeLock
import com.das.mediaHub.python.YouTuber.loadStreamUrl
import com.das.mediaHub.data.local.DatabaseFavorite
import com.das.mediaHub.data.local.WatchHistory
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.model.VideoDetails
import com.das.mediaHub.data.model.VideosListData
import com.das.mediaHub.data.icons.YouTube
import com.das.mediaHub.data.model.searcher.Video
import com.das.mediaHub.mediacontroller.VideoPlayerListener
import com.das.mediaHub.player.video.VideoPlayerManager
import com.das.mediaHub.ui.players.videoPlayer.CustomMethods.SkeletonSuggestionLoadingLayout
import com.das.mediaHub.ui.players.videoPlayer.CustomMethods.SkeletonLoadingLayout
import com.das.mediaHub.ui.players.videoPlayer.CustomMethods.setFullscreen
import com.das.mediaHub.ui.players.videoPlayer.state.VideoUiState
import com.das.mediaHub.ui.theme.AppTheme
import com.das.mediaHub.ui.theme.ThemePreferences.loadDarkModeState
import kotlinx.coroutines.launch


@OptIn(UnstableApi::class)
@Composable
fun MainActivity.OnlineVideoPlayer(
    backStack: NavBackStack<NavKey>,
    data: Video
) {

    val isInFullScreen = remember { mutableStateOf(false) }
    val showAlertDialog = remember { mutableStateOf(false) }


    val viewModel = viewModel<ViewerViewModel>()


    val videoID = data.id

    val videoPlayerManager = remember {
        VideoPlayerManager(
            VideoPlayerListener(
                this, backStack
            )
        )
    }


    val mExoPlayer = videoPlayerManager.player
    var videoUiState by remember {
        mutableStateOf(VideoUiState.from(data))
    }

    val suggestionsState by viewModel.suggestionsState.collectAsState()
    val isLoadingVideos = suggestionsState.isLoading
    val videosListResult = suggestionsState.data.orEmpty()
    val suggestionError = suggestionsState.error

    val videoState by viewModel.videoState.collectAsState()

    val videoUrl = videoState.data.orEmpty()
    val isLoading = videoState.isLoading
    val isThereError = videoState.error

    LaunchedEffect(videoID) {
        viewModel.loadDetails(videoID)
    }


    LaunchedEffect(videoUiState.title) {
        if (!videoUiState.title.isNullOrEmpty()) {
            viewModel.fetchSuggestions(videoUiState.title!!)

        }
    }

    DisposableEffect(Unit) {
        acquireWakeLock()

        onDispose {
            releaseWakeLock()
            setFullscreen(false)
            shouldEnterPipMode.value = false
            videoPlayerManager.release()
        }
    }

    LaunchedEffect(videoUrl, isLoading) {
        if (videoUrl.isNotEmpty() && !isLoading) {
            if (videoPlayerManager.isEmptyMediaItem) {
                videoPlayerManager.playVideo(
                    videoUrl.toUri()
                )
            }
        }
    }

    val isInPipMode = rememberIsInPipMode()


    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddings ->

        Column(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize()
        ) {
            if (videoUrl.isNotEmpty() && !isLoading) {


                val playerModifier = if (isInFullScreen.value) {
                    Modifier
                        .fillMaxSize()
                } else {
                    Modifier
                        .height(220.dp)
                        .fillMaxWidth()
                }


                @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
                AndroidView(
                    modifier = playerModifier
                        .background(Color.Black)
                        .then(
                            if (isInPipMode) Modifier.fillMaxSize() else Modifier
                        ),
                    factory = {
                        PlayerView(it).apply {
                            player = mExoPlayer
                            keepScreenOn = true
                        }
                    },
                    update = { playerView ->
                        playerView.player = mExoPlayer
                        playerView.setFullscreenButtonState(isInFullScreen.value)
                        playerView.setFullscreenButtonClickListener {
                            isInFullScreen.value = it
                        }
                        playerView.resizeMode = if (isInFullScreen.value)
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        else
                            AspectRatioFrameLayout.RESIZE_MODE_FIT
                        playerView.useController = !isInPipMode
                    }
                )
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .height(height = 220.dp)
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                Box(
                    modifier = Modifier
                        .height(220.dp)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error message",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = isThereError.toString(),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth(0.87f)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                }

            }
            if (!isInFullScreen.value) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {

                    item(videoID) {
                        VideoDetailsComposable(
                            videoId = videoID,
                            channelThumbnailURL = videoUiState.channelThumbnail ?: "none is here",
                            duration = videoUiState.duration ?: "0:00",
                            viewModel = viewModel,
                            clickForMore = {
                                showAlertDialog.value = true
                            },
                            downloadAsVideo = {
                                startDownloadingVideo(videoID, it)

                            },
                            downloadAsMusic = {
                                startDownloadingAudio(videoID, it)

                            },
                            finished = {
                                videoUiState = videoUiState.copy(
                                    title = it.title,
                                    views = it.viewNumber,
                                    date = it.date,
                                    channelName = it.channelName
                                )
                            }

                        ) {
                            videoPlayerManager.pause()
                            val currentTimeSec = (mExoPlayer.currentPosition) / 1000
                            val youtubeUrl =
                                "https://www.youtube.com/watch?v=${videoID}&t=${currentTimeSec}s".toUri()

                            try {
                                val intent = Intent(Intent.ACTION_VIEW, youtubeUrl).apply {
                                    setPackage("com.google.android.youtube")
                                }
                                startActivity(intent)
                            } catch (_: Exception) {
                                openCustomTab(youtubeUrl)
                            }
                        }

                    }

                    if (isLoadingVideos) {
                        item {
                            SkeletonSuggestionLoadingLayout()
                        }
                    } else if (!suggestionError.isNullOrEmpty() || videosListResult.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = suggestionError ?: "can't find any videos",
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(videosListResult) { searchItem ->
                            if (searchItem.id == videoID) {
                                videoUiState = videoUiState.copy(
                                    channelThumbnail = searchItem.channel?.thumbnails?.firstOrNull()?.url,
                                    duration = searchItem.duration
                                )
                            }
                            VideoLists(
                                backStack,
                                searchItem
                            ) {
                                if (it) {
                                    startDownloadingVideo(searchItem.id, searchItem.title ?: "")
                                } else {
                                    startDownloadingAudio(searchItem.id, searchItem.title ?: "")
                                }
                            }
                            LaunchedEffect(searchItem.id) {
                                listOfVideosListData.add(searchItem)
                            }

                        }

                    }
                }
            }
        }
    }


    LaunchedEffect(isInFullScreen.value) {
        setFullscreen(isInFullScreen.value)
    }


    AskToPlay(
        showAlertDialog = showAlertDialog.value,
        mContext = this,
        videoUrl,
        videoID,
        videoUiState,
        onDismissRequest = {
            showAlertDialog.value = false
        }
    )
}


@Composable
private fun VideoDetailsComposable(
    videoId: String,
    channelThumbnailURL: String,
    duration: String,
    viewModel: ViewerViewModel,
    clickForMore: () -> Unit,
    downloadAsVideo: (videoTitle: String) -> Unit,
    downloadAsMusic: (title: String) -> Unit,
    finished: (title: VideoDetails) ->Unit,
    playItInYouTube: () -> Unit
) {
    val mContext = LocalContext.current

    val showDescriptionDialog = remember { mutableStateOf(false) }
    val comingSoonDialog = remember { mutableStateOf(false) }
    val detailsState by viewModel.detailsState.collectAsState()
    val isLoading = detailsState.isLoading
    val error = detailsState.error

    val dbForFav = remember {
        DatabaseFavorite(mContext)
    }
    val watchHistory = remember {
        WatchHistory(mContext)
    }

    val videoDetails = detailsState.data
    var isSaved by remember {
        mutableStateOf(
            dbForFav.isWatchUrlExist(videoId)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {


        if (isLoading || videoDetails == null) {
            SkeletonLoadingLayout()

        } else if (error != null) {

            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            videoDetails.let {

                val title = it.title
                LaunchedEffect(title) {
                    finished(it)
                    if (channelThumbnailURL != "none is here") {
                        watchHistory.insertNewVideo(
                            videoId,
                            title,
                            it.date,
                            it.viewNumber,
                            it.channelName,
                            duration,
                            channelThumbnailURL
                        )
                    }
                }



                Text(
                    text = title,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(onClick = {
                            showDescriptionDialog.value = true
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
                            .data(data=channelThumbnailURL)
                            .crossfade(true)
                            .error(R.mipmap.under_development)
                            .build(),
                        contentDescription = "Category Image",
                        modifier = Modifier
                            .size(34.dp, 34.dp)
                            .clip(RoundedCornerShape(50))
                            .combinedClickable(
                                onClick = {
                                    comingSoonDialog.value = true
                                }
                            ),
                        alignment = Alignment.CenterStart,
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = it.channelName,
                        maxLines = 1,
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .width(142.dp)
                            .align(Alignment.CenterVertically)
                            .padding(bottom = 3.dp)
                    )

                    Text(
                        text = it.viewNumber,
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
                        text = it.date,
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
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .fillMaxWidth()
                ) {
                    OutlinedIconButton (
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "https://youtu.be/${videoId}?feature=shared"
                                )
                            }

                            val chooser = Intent.createChooser(shareIntent, "Share via")
                            mContext.startActivity(chooser)
                        },
                        shape = RoundedCornerShape(25)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Share),
                            "Share the video"
                        )
                    }
                    OutlinedIconButton(
                        onClick = {
                            if (isSaved) {
                                dbForFav.deleteWatchUrl(videoId)
                                isSaved = false
                            } else {
                                dbForFav.insertData(
                                    videoId,
                                    title,
                                    it.date,
                                    it.viewNumber,
                                    it.channelName,
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
                    OutlinedIconButton(
                        onClick = {
                            downloadAsMusic(title)
                        },
                        shape = RoundedCornerShape(25)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.MusicNote),
                            "Download the video as music"
                        )
                    }

                    OutlinedIconButton(
                        onClick = {
                            downloadAsVideo(
                                title
                            )
                        },
                        shape = RoundedCornerShape(25)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Videocam),
                            "Download video as video"
                        )
                    }

                    OutlinedIconButton(
                        onClick = {
                            playItInYouTube()
                        },
                        shape = RoundedCornerShape(25)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(YouTube),
                            tint = Color.Red,
                            contentDescription = "Play in YouTube"
                        )
                    }

                    OutlinedIconButton(
                        onClick = {
                            clickForMore()
                        },
                        shape = RoundedCornerShape(25)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.AutoMirrored.Default.More),
                            "Click here for more options"
                        )
                    }
                }
                if (showDescriptionDialog.value) {
                    ShowDescriptionDialog(
                        it.description
                    ) {
                        showDescriptionDialog.value = false
                    }
                }
            }
        }
    }


    if (comingSoonDialog.value){

        ComingSoonAlertDialog {
            comingSoonDialog.value = false
        }
    }

}




@Composable
private fun VideoLists(
    backStack: NavBackStack<NavKey>,
    searchItem: Video,
    downloadNow: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val videoId = searchItem.id
    val title = searchItem.title
    val viewsNumber = searchItem.viewCount?.short ?: "0"
    val dateOfVideo = searchItem.publishedTime ?: ""
    val channelName = searchItem.channel?.name ?: ""
    val duration = searchItem.duration ?: "0"
    val videoThumbnailURL = searchItem.id
    val channelThumbnails = searchItem.channel?.thumbnails?.get(0)?.url ?: ""

    val showDialog = remember { mutableStateOf(false) }
    val showInfoDialog = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(1))
            .fillMaxWidth()
            .padding(bottom = 3.dp, top = 3.dp)
            .combinedClickable(
                onClick = {
                    playThisOne(
                        backStack = backStack,
                        videosListDataDetails = searchItem
                    )
                },
                onLongClick = {
                    showDialog.value = true
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
                        showInfoDialog.value = true
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
                        text = title ?: "",
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
                        showDialog.value = true
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


    if (showInfoDialog.value){
        AlertDialogForUser{
            showInfoDialog.value = false
        }

    }

    if (showDialog.value) {
        ShowAlertDialog(
            mContext = context,
            selectedItem = VideosListData(
                videoId, title ?: "", viewsNumber, dateOfVideo,
                duration, channelName, channelThumbnails
            ),
            onDismissRequest = {
                showDialog.value = false
                if (it != null) {
                    downloadNow(it)
                }
            }
        )
    }
}




fun playThisOne(
    backStack: NavBackStack<NavKey>,
    gotIndex: Int = 1,
    videosListDataDetails: Video = listOfVideosListData[gotIndex]
) {
    backStack.removeLastOrNull()
    backStack.add(NavScreens.VideoViewer(videosListDataDetails))


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
    val themeState by loadDarkModeState()

    val isDarkTheme = when (themeState) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val linkColor = if (isDarkTheme) Color(0xFF1565C0) else Color(0xFF64B5F6)

    val context = LocalContext.current


    val urlPattern = """https?://\S+""".toRegex()
    val matches = urlPattern.findAll(text)

    val annotation = AnnotatedString.Builder(text)

    matches.forEach { match ->
        val url = match.value
        val startIndex = match.range.first
        val endIndex = match.range.last + 1

        annotation.addLink(
            LinkAnnotation.Clickable(
                tag = url,
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                ),
                linkInteractionListener = {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                }
            ),
            startIndex,
            endIndex
        )
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
                        text = annotation.toAnnotatedString(),
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
    onDismissRequest: (Boolean?) -> Unit
) {
    val thumbnailUrl = "https://img.youtube.com/vi/${selectedItem.videoId}/0.jpg"

    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    if (isLoading.value) {
        Dialog(
            onDismissRequest = { /* block user from dismissing while loading */ },
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Fetching stream URL...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    if (errorMessage.value != null) {
        AlertDialog(
            onDismissRequest = {
                errorMessage.value = null
                onDismissRequest(null)
            },
            confirmButton = {
                TextButton(onClick = {
                    errorMessage.value = null
                    onDismissRequest(null)
                }) { Text("OK") }
            },
            title = { Text("Error") },
            text = { Text(errorMessage.value ?: "Unknown error") }
        )
    }

    if (!isLoading.value && errorMessage.value == null) {
        Dialog(onDismissRequest = { onDismissRequest(null) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(13.dp)
                    .dropShadow(
                        RoundedCornerShape(16),
                        shadow = Shadow(25.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Do you want to download it as video or audio, or play it in background?",
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(mContext)
                            .data(thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Thumbnail",
                        modifier = Modifier
                            .height(190.dp)
                            .clip(RoundedCornerShape(4)),
                        contentScale = ContentScale.Fit
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(
                            onClick = {
                                isLoading.value = true
                                scope.launch {
                                    selectedItem.loadStreamUrl(
                                        onSuccess = {
                                            mContext.playAudioFromUrl(audioUrl = it.audioUrl, selectedItem = it)
                                            isLoading.value = false
                                            onDismissRequest(null)
                                        },
                                        onFailure = { err ->
                                            errorMessage.value = err.message
                                            isLoading.value = false
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.padding(4.dp),
                        ) {
                            Text("Background")
                        }

                        TextButton(
                            onClick = {
                                onDismissRequest(false)
                            },
                            modifier = Modifier.padding(4.dp),
                        ) {
                            Text("Music")
                        }

                        TextButton(
                            onClick = {
                                onDismissRequest(true)
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
}

/** Hello**
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
            selectedItem.loadStreamUrl(
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
                .padding(13.dp)
                .dropShadow(
                    RoundedCornerShape(16),
                    shadow = Shadow(25.dp)
                ),
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



*/





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
    showAlertDialog: Boolean,
    mContext: Context,
    url: String,
    id: String,
    video: VideoUiState,
    onDismissRequest: () -> Unit
) {
    val playIntent = Intent(mContext, AudioServiceFromUrl::class.java).apply {
        action = ACTION_START
        putExtra("videoId", id)
        putExtra("media_url", url)
        putExtra("title", video.title)
        putExtra("channelName", video.channelName)
        putExtra("viewNumber", video.views)
        putExtra("videoDate", video.date)
        putExtra("duration", video.duration)
    }
    if (showAlertDialog) {
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

}
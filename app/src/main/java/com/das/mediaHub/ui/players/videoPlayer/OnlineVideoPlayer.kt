package com.das.mediaHub.ui.players.videoPlayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.More
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.YouTubeIcon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import com.das.mediaHub.services.AudioServiceFromUrl
import com.das.mediaHub.MainActivity
import com.das.mediaHub.data.constants.GlobalVideoList.listOfVideosListData
import com.das.mediaHub.NavScreens
import com.das.mediaHub.OnLaunchComponents.openCustomTab
import com.das.mediaHub.OnLaunchComponents.playAudioFromUrl
import com.das.mediaHub.PIP.rememberIsInPipMode
import com.das.mediaHub.WakeLockHelper.acquireWakeLock
import com.das.mediaHub.WakeLockHelper.releaseWakeLock
import com.das.mediaHub.python.YouTuber.loadStreamUrl
import com.das.mediaHub.data.local.DatabaseFavorite
import com.das.mediaHub.data.local.WatchHistory
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.model.VideoDetails
import com.das.mediaHub.data.model.VideosListData
import com.das.mediaHub.data.model.searcher.Video
import com.das.mediaHub.mediacontroller.VideoPlayerListener
import com.das.mediaHub.player.video.VideoPlayerManager
import com.das.mediaHub.ui.players.videoPlayer.CustomMethods.SkeletonSuggestionLoadingLayout
import com.das.mediaHub.ui.players.videoPlayer.CustomMethods.SkeletonLoadingLayout
import com.das.mediaHub.ui.players.videoPlayer.CustomMethods.setFullscreen
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import com.das.mediaHub.ui.players.videoPlayer.state.VideoUiState
import com.das.mediaHub.ui.theme.AppTheme
import com.das.mediaHub.ui.theme.ThemePreferences.loadDarkModeState
import kotlinx.coroutines.launch

@Composable
fun MainActivity.OnlineVideoPlayer(
    backStack: NavBackStack<NavKey>,
    data: Video
) {
    val isInFullScreen = remember { mutableStateOf(false) }
    val showAlertDialog = remember { mutableStateOf(false) }

    val videoID = data.id

    val viewModel = viewModel(modelClass = ViewerViewModel::class.java, key = "VideoViewer_$videoID")
    val videoState by viewModel.videoState.collectAsState()
    val videoUrl = videoState.data

    LaunchedEffect(Unit) {
        viewModel.loadDetails(videoID)
    }

    val videoPlayerManager = remember {
        VideoPlayerManager(
            VideoPlayerListener(
                this, backStack
            )
        )
    }

    LaunchedEffect(videoUrl, videoState.isLoading) {
        if (!videoUrl.isNullOrEmpty() && !videoState.isLoading) {
            if (videoPlayerManager.isEmptyMediaItem) {
                videoPlayerManager.playVideo(
                    videoUrl.toUri()
                )
            }
        }
    }
    val mExoPlayer = videoPlayerManager.player
    var videoUiState by remember {
        mutableStateOf(VideoUiState.from(data))
    }
    val suggestionsState by viewModel.suggestionsState.collectAsState()

    val isLoadingVideos = suggestionsState.isLoading
    val videosListResult = suggestionsState.data.orEmpty()
    val suggestionError = suggestionsState.error

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
            videoPlayerManager.release()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddings ->
        Column(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black,
                            MaterialTheme.colorScheme.surface
                        ),
                        startY = 0f,
                        endY = 500f
                    )
                )
        ) {
            VideoScreen(
                videoState = videoState, mExoPlayer = mExoPlayer,
                isInFullScreen.value
            ) {
                isInFullScreen.value = it
            }
            if (!isInFullScreen.value) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
//                    contentPadding = PaddingValues(bottom = 16.dp)
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
                            val youtubeUrl = "https://www.youtube.com/watch?v=${videoID}&t=${currentTimeSec}s".toUri()

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

                    item {
                        Text(
                            text = "Up Next",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                        )
                    }

                    if (isLoadingVideos) {
                        item {
                            SkeletonSuggestionLoadingLayout()
                        }
                    } else if (!suggestionError.isNullOrEmpty() || videosListResult.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = suggestionError ?: "Can't find any related videos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            VideoCard(
                                backStack,
                                searchItem
                            ) { isVideo ->
                                if (isVideo) {
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
        videoUrl ?: "",
        videoID,
        videoUiState,
        onDismissRequest = {
            showAlertDialog.value = false
        }
    )
}

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun MainActivity.VideoScreen(
    videoState: UiState<String>,
    mExoPlayer: ExoPlayer,
    isInFullScreen: Boolean,
    fullScreen: (Boolean) -> Unit
) {
    val videoUrl = videoState.data
    val isLoading = videoState.isLoading
    val isThereError = videoState.error
    val isInPipMode = rememberIsInPipMode()

    val playerModifier = if (isInFullScreen) {
        Modifier.fillMaxSize()
    } else {
        Modifier
            .fillMaxWidth()
            .height(230.dp)
    }

    if (!videoUrl.isNullOrEmpty() && !isLoading) {
        AndroidView(
            modifier = playerModifier
                .background(Color.Black)
                .then(if (isInPipMode) Modifier.fillMaxSize() else Modifier),
            factory = {
                PlayerView(it).apply {
                    player = mExoPlayer
                    keepScreenOn = true
                }
            },
            update = { playerView ->
                playerView.player = mExoPlayer
                playerView.setFullscreenButtonState(isInFullScreen)
                playerView.setFullscreenButtonClickListener {
                    fullScreen(it)
                }
                playerView.resizeMode = if (isInFullScreen)
                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                else
                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                playerView.useController = !isInPipMode
            }
        )
    } else if (isThereError != null) {
        Box(
            modifier = playerModifier.background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = isThereError,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    } else {
        Box(
            modifier = playerModifier.background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
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
    finished: (title: VideoDetails) -> Unit,
    playItInYouTube: () -> Unit
) {
    val mContext = LocalContext.current
    val showDescriptionDialog = remember { mutableStateOf(false) }
    val detailsState by viewModel.detailsState.collectAsState()
    val isLoading = detailsState.isLoading
    val error = detailsState.error

    val dbForFav = remember { DatabaseFavorite(mContext) }
    val watchHistory = remember { WatchHistory(mContext) }

    val videoDetails = detailsState.data
    var isSaved by remember { mutableStateOf(dbForFav.isWatchUrlExist(videoId)) }

    if (!isLoading && videoDetails != null && error == null) {
        val title = videoDetails.title
        LaunchedEffect(title) {
            finished(videoDetails)
            if (channelThumbnailURL != "none is here") {
                watchHistory.insertNewVideo(
                    videoId, title, videoDetails.date,
                    videoDetails.viewNumber, videoDetails.channelName,
                    duration, channelThumbnailURL
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                ),
                maxLines = 3,
                modifier = Modifier.clickable { showDescriptionDialog.value = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = channelThumbnailURL,
                    error = rememberVectorPainter(Icons.Default.Error),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = videoDetails.channelName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${videoDetails.viewNumber} • ${videoDetails.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionIconButton(
                    icon = if (isSaved) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    label = if (isSaved) "Saved" else "Save",
                    tint = if (isSaved) Color.Red else MaterialTheme.colorScheme.onSurface
                ) {
                    if (isSaved) {
                        dbForFav.deleteWatchUrl(videoId)
                        isSaved = false
                    } else {
                        dbForFav.insertData(
                            videoId, title, videoDetails.date,
                            videoDetails.viewNumber, videoDetails.channelName,
                            duration, channelThumbnailURL
                        )
                        isSaved = true
                    }
                }

                ActionIconButton(icon = Icons.Default.Share, label = "Share") {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "https://youtu.be/${videoId}")
                    }
                    mContext.startActivity(Intent.createChooser(shareIntent, "Share via"))
                }

                ActionIconButton(icon = Icons.Default.MusicNote, label = "MP3") {
                    downloadAsMusic(title)
                }

                ActionIconButton(icon = Icons.Default.Videocam, label = "MP4") {
                    downloadAsVideo(title)
                }

                ActionIconButton(icon = Icons.Default.YouTubeIcon, label = "YouTube", tint = Color.Red) {
                    playItInYouTube()
                }

                ActionIconButton(icon = Icons.AutoMirrored.Default.More, label = "More") {
                    clickForMore()
                }
            }
        }

        if (showDescriptionDialog.value) {
            ShowDescriptionDialog(videoDetails.description) {
                showDescriptionDialog.value = false
            }
        }
    } else if (error != null) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
    } else {
        SkeletonLoadingLayout()
    }
}

@Composable
fun ActionIconButton(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VideoCard(
    backStack: NavBackStack<NavKey>,
    searchItem: Video,
    downloadNow: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val videoId = searchItem.id
    val title = searchItem.title ?: ""
    val viewsNumber = searchItem.viewCount?.short ?: "0"
    val dateOfVideo = searchItem.publishedTime ?: ""
    val channelName = searchItem.channel?.name ?: ""
    val duration = searchItem.duration ?: "0:00"
    val channelThumbnails = searchItem.channel?.thumbnails?.firstOrNull()?.url ?: ""

    val showDialog = remember { mutableStateOf(false) }

    Card(
        onClick = {
            backStack.removeLastOrNull()
            backStack.add(NavScreens.VideoViewer(searchItem))
        },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            Box(
                modifier = Modifier
                    .size(160.dp, 90.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = "https://img.youtube.com/vi/$videoId/0.jpg",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = channelName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "$viewsNumber • $dateOfVideo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = { showDialog.value = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }

    if (showDialog.value) {
        ShowAlertDialog(
            mContext = context,
            selectedItem = VideosListData(
                videoId, title, viewsNumber, dateOfVideo,
                duration, channelName, channelThumbnails
            ),
            onDismissRequest = {
                showDialog.value = false
                if (it != null) downloadNow(it)
            }
        )
    }
}

@Composable
private fun ShowDescriptionDialog(text: String, onDismissRequest: () -> Unit) {
    val themeState by loadDarkModeState()
    val isDarkTheme = when (themeState) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val linkColor = if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF1565C0)
    val context = LocalContext.current
    val urlPattern = """https?://\S+""".toRegex()
    val matches = urlPattern.findAll(text)
    val annotation = AnnotatedString.Builder(text)

    matches.forEach { match ->
        val url = match.value
        annotation.addLink(
            LinkAnnotation.Clickable(
                tag = url,
                styles = TextLinkStyles(style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
                linkInteractionListener = { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
            ),
            match.range.first, match.range.last + 1
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Description", fontWeight = FontWeight.Bold) },
        text = {
            SelectionContainer {
                Box(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = annotation.toAnnotatedString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismissRequest) { Text("Close") } }
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
        Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Fetching stream URL...")
                }
            }
        }
    }

    if (errorMessage.value != null) {
        AlertDialog(
            onDismissRequest = { errorMessage.value = null; onDismissRequest(null) },
            confirmButton = { TextButton(onClick = { errorMessage.value = null; onDismissRequest(null) }) { Text("OK") } },
            title = { Text("Error") },
            text = { Text(errorMessage.value!!) }
        )
    }

    if (!isLoading.value && errorMessage.value == null) {
        Dialog(onDismissRequest = { onDismissRequest(null) }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choose Action",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                isLoading.value = true
                                scope.launch {
                                    selectedItem.loadStreamUrl(
                                        onSuccess = {
                                            mContext.playAudioFromUrl(it.audioUrl, it)
                                            isLoading.value = false
                                            onDismissRequest(null)
                                        },
                                        onFailure = { err ->
                                            errorMessage.value = err.message
                                            isLoading.value = false
                                        }
                                    )
                                }
                            }
                        ) { Text("Background") }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { onDismissRequest(false) },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Music") }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { onDismissRequest(true) },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Video") }
                    }
                }
            }
        }
    }
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
    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Background Play") },
            text = { Text("Do you want to continue playing this media in the background?") },
            confirmButton = {
                TextButton(onClick = {
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
                    ContextCompat.startForegroundService(mContext, playIntent)
                    onDismissRequest()
                }) { Text("Yes") }
            },
            dismissButton = { TextButton(onClick = onDismissRequest) { Text("No") } }
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
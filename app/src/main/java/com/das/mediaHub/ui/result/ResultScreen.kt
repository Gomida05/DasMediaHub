package com.das.mediaHub.ui.result

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.das.downloader.data.model.download.DownloadType
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.data.model.icons.filled.YouTubeIcon
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.navigation.AppBackStack
import com.das.mediaHub.navigation.Destination.OnlineVideoPlayer
import com.das.mediaHub.services.download.DownloadService
import com.das.mediaHub.services.media.online.OnlineBackgroundPlayer.Companion.playAudioFromUrl
import com.das.mediaHub.ui.components.ErrorStateView
import com.das.mediaHub.ui.components.dialogs.ActionMenuItem
import com.das.mediaHub.ui.notification.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.players.videoPlayer.ActionDialogState
import com.das.mediaHub.ui.players.videoPlayer.ActionStatusDialog
import com.das.mediaHub.ui.players.videoPlayer.components.CustomLayouts.SkeletonSuggestionLoadingLayout
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.openCustomTab
import com.das.mediaHub.ui.players.videoPlayer.components.VideoActionMenu
import com.das.python.data.model.searcher.Video
import kotlinx.coroutines.launch

@Composable
fun ResultScreen(backStack: AppBackStack, data: String) {

    val context = LocalContext.current
    val viewModel = viewModel(modelClass = ResultViewModel::class.java.kotlin)

    val searchResultsState by viewModel.searchResults.collectAsStateWithLifecycle()

    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()


    val snackBar = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()

    var dialogState by remember { mutableStateOf<ActionDialogState>(ActionDialogState.Idle) }

    LaunchedEffect(data) {
        viewModel.loadInitialIfNeeded(data)
    }


    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackBar) },
            containerColor = Color.Transparent,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    title = {},
                    actions = {
                        // Centered and width-constrained for tablets
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                onClick = { backStack.removeLastOrNull() },
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .widthIn(max = 600.dp) // <-- Prevents ultra-wide stretching on large screens
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 10.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = data,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->

            // Replaced LazyColumn with LazyVerticalGrid for adaptive columns
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp), // Automatically handles 1 col on mobile, 2+ on tablet
                state = lazyGridState,
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                when (val state = searchResultsState) {
                    UiState.Idle,
                    UiState.Loading -> {
                        // GridItemSpan(maxLineSpan) forces this item to take the full width of the screen
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                SkeletonSuggestionLoadingLayout(true)
                            }
                        }
                    }

                    UiState.Empty -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ResultStateCard(
                                title = "No results found",
                                message = "We couldn’t find anything for \"$data\". Try another keyword or a simpler search.",
                                icon = Icons.Outlined.Search
                            )
                        }
                    }

                    is UiState.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ErrorStateView(
                                title = "Something went wrong",
                                message = state.message,
                                onRetry = { viewModel.retry(data) },

                            )
                        }
                    }

                    is UiState.Success -> {
                        itemsIndexed(
                            items = state.data,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            if (index >= state.data.size - 3 && !isLoadingMore) {
                                LaunchedEffect(index) { viewModel.loadMore() }
                            }

                            VideoResultItem(
                                backStack = backStack,
                                searchItem = item,
                                playItInYouTube = {
                                    val youtubeUrl =
                                        "https://www.youtube.com/watch?v=${item.id}".toUri()

                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, youtubeUrl).apply {
                                            setPackage("com.google.android.youtube")
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        context.openCustomTab(youtubeUrl)
                                    }
                                },
                                playItInBackground = {
                                    viewModel.loadStreamUrl(
                                        mediaItem = item,
                                        onStart = {
                                            dialogState = ActionDialogState.Loading
                                        },
                                        onSuccess = { streamResult ->
                                            dialogState = ActionDialogState.Idle

                                            if (streamResult.audioUrl.isBlank()) {
                                                dialogState =
                                                    ActionDialogState.Error("This video can’t be played in the background right now.")
                                                return@loadStreamUrl
                                            }

                                            context.playAudioFromUrl(
                                                audioUrl = streamResult.audioUrl,
                                                selectedItem = streamResult
                                            )
                                        },
                                        onFailure = {
                                            dialogState =
                                                ActionDialogState.Error("Couldn't start background play. Please try again.")
                                        }
                                    )
                                },
                                downloadIt = { id, title, type ->
                                    DownloadService.startForYouTube(
                                        context = context,
                                        id = id,
                                        title = title,
                                        type = type
                                    )

                                }
                            ) {
                                scope.launch { snackBar.showSnackbar(it) }
                            }
                        }

                        if (isLoadingMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            ActionStatusDialog(dialogState) {
                dialogState = ActionDialogState.Idle
            }
        }
    }
}


@Composable
fun VideoResultItem(
    backStack: AppBackStack,
    searchItem: Video,
    playItInYouTube: () -> Unit,
    playItInBackground: () -> Unit,
    downloadIt: (
        id: String,
        title: String,
        type: DownloadType
    ) -> Unit,
    snackBar: (String) -> Unit
) {

    val videoId = searchItem.id
    val title = searchItem.title ?: ""
    val viewsNumber = searchItem.viewCount?.short ?: "0"
    val dateOfVideo = searchItem.publishedTime ?: ""
    val channelName = searchItem.channel?.name ?: ""
    val duration = searchItem.duration ?: "0:00"
    val channelThumbnails = searchItem.channel?.thumbnails?.get(0)?.url ?: ""

    val showMenu = remember { mutableStateOf(false) }
    val showDownloadType = remember { mutableStateOf(false) }

    Card(
        onClick = { backStack.add(OnlineVideoPlayer(videoId = videoId)) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f) // <-- Crucial adaptive change: Maintains perfect YouTube aspect ratio instead of strict 200.dp
                    .clip(RoundedCornerShape(20.dp))
            ) {
                AsyncImage(
                    model = "https://img.youtube.com/vi/$videoId/0.jpg",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = duration,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = channelThumbnails,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { snackBar(channelName) },
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$channelName • $viewsNumber • $dateOfVideo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { showMenu.value = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    VideoActionMenu(
                        title = title,
                        expanded = showMenu.value,
                        onDismissRequest = { showMenu.value = false }
                    ) { _, _ ->

                        ResultScreenActionItems(
                            title,
                            channelName,
                            playItHere = {
                                backStack.add(OnlineVideoPlayer(videoId = videoId))
                            },
                            playItInYouTube = playItInYouTube,
                            showDownloadType = {
                                showDownloadType.value = true
                            },
                            playItInBackground = playItInBackground,
                            { showMenu.value = false }
                        )
                    }
                }
            }
        }
    }

    if (showDownloadType.value) {
        DownloadTypeDialog(
            mediaTitle = title,
            onDismiss = { type ->
                showDownloadType.value = false

                type?.let {
                    downloadIt(
                        videoId,
                        title,
                        it
                    )
                }
            }
        )
    }


}




@Composable
fun ResultStateCard(
    title: String,
    message: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    icon: ImageVector = Icons.Outlined.Search,
    isError: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = if (isError) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                },
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}


@Composable
fun ResultScreenActionItems(
    channelName: String,
    title: String,
    playItHere: () -> Unit,
    playItInYouTube: () -> Unit,
    showDownloadType: () -> Unit,
    playItInBackground: () -> Unit,
    onDismissRequest: () -> Unit,
) {

    ActionMenuItem(
        icon = Icons.Default.PlayArrow,
        title = "Open video",
        subtitle = "Watch now",
        onClick = {
            onDismissRequest()
            playItHere()
        }
    )

    ActionMenuItem(
        icon = Icons.Default.YouTubeIcon,
        title = "Open in YouTube",
        subtitle = "Watch in official app",
        iconColor = Color.Red,
        onClick = {
            onDismissRequest()
            playItInYouTube()
        }
    )

    // 🎧 Background play
    ActionMenuItem(
        icon = Icons.Default.Headphones,
        title = "Play in background",
        subtitle = "Audio only",
        onClick = {
            onDismissRequest()
            playItInBackground()
        }
    )

    ActionMenuItem(
        icon = Icons.Default.Download,
        title = "Download it",
        subtitle = title,
        onClick = {
            onDismissRequest()
            showDownloadType()
        }
    )

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
    // ℹ️ Details
    ActionMenuItem(
        icon = Icons.Default.Info,
        title = "Video details",
        subtitle = channelName.ifBlank { "More info" },
        onClick = {
            onDismissRequest()
            showNotificationDialog = TopPopUp(
                message = "More video details are coming soon.",
                icon = Icons.Default.Info,
                loading = false
            )
        }
    )
}

@Composable
fun DownloadTypeDialog(
    mediaTitle: String,
    onDismiss: (DownloadType?) -> Unit
) {
    Dialog(
        onDismissRequest = {
            onDismiss(null)
        }
    ) {
        Surface(
            shape = RoundedCornerShape(34.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 12.dp,
            shadowElevation = 20.dp,
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {

            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // ✨ Header
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                    Text(
                        text = "Download",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = mediaTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )

                // 🎵 AUDIO CARD
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onDismiss(DownloadType.MUSIC)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LibraryMusic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Audio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "MP3 / M4A • smaller size",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 🎬 VIDEO CARD
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onDismiss(DownloadType.VIDEO)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VideoFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Video",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "HD quality • larger file",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ❌ Cancel
                TextButton(
                    onClick = {
                        onDismiss(null)
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
package com.das.mediaHub.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import com.das.mediaHub.navigation.NavScreens.OnlineVideoPlayer
import com.das.mediaHub.services.media.OnlineBackgroundPlayer.Companion.playAudioFromUrl
import com.das.downloader.data.model.download.DownloadType
import com.das.mediaHub.services.download.DownloadService
import com.das.mediaHub.ui.players.videoPlayer.components.CustomLayouts.SkeletonSuggestionLoadingLayout
import com.das.mediaHub.data.model.state.UiState
import com.das.python.YouTuber.loadStreamUrl
import com.das.python.data.model.VideosListData
import com.das.python.data.model.searcher.Video
import kotlinx.coroutines.launch

@Composable
fun ResultViewerPage(backStack: NavBackStack<NavKey>, data: String) {

    val viewModel = viewModel(modelClass = ResultViewModel::class.java.kotlin)

    val searchResultsState by viewModel.searchResults.collectAsStateWithLifecycle()

    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()


    val snackBar = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val lazyState = rememberLazyListState()


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
            snackbarHost = {
                SnackbarHost(snackBar)
            },
            containerColor = Color.Transparent,
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    title = {},
                    actions = {
                        Surface(
                            onClick = {
                                backStack.removeLastOrNull()
                            },
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
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
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        )
        { paddingValues ->
            LazyColumn(
                state = lazyState,
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                when (val state = searchResultsState) {
                    UiState.Idle,
                    UiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                SkeletonSuggestionLoadingLayout(true)
                            }
                        }
                    }

                    UiState.Empty -> {
                        item {
                            ResultStateCard(
                                title = "No results found",
                                message = "We couldn’t find anything for \"$data\". Try another keyword or a simpler search.",
                                icon = Icons.Outlined.Search
                            )
                        }
                    }

                    is UiState.Error -> {
                        item {
                            ResultStateCard(
                                title = "Something went wrong",
                                message = state.message,
                                actionLabel = "Retry",
                                onActionClick = {
                                    viewModel.retry(data)
                                },
                                icon = Icons.Outlined.ErrorOutline,
                                isError = true
                            )
                        }
                    }

                    is UiState.Success -> {
                        itemsIndexed(
                            items = state.data,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            if (index >= state.data.size - 3 && !isLoadingMore) {
                                LaunchedEffect(index) {
                                    viewModel.loadMore()
                                }
                            }

                            VideoResultItem(
                                backStack = backStack,
                                searchItem = item
                            ) {
                                scope.launch {
                                    snackBar.showSnackbar(it)
                                }
                            }
                        }

                        if (isLoadingMore) {
                            item {
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

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoResultItem(
    backStack: NavBackStack<NavKey>,
    searchItem: Video,
    snackBar: (String) -> Unit
) {
    val videoId = searchItem.id
    val title = searchItem.title ?: ""
    val viewsNumber = searchItem.viewCount?.short ?: "0"
    val dateOfVideo = searchItem.publishedTime ?: ""
    val channelName = searchItem.channel?.name ?: ""
    val duration = searchItem.duration ?: "0:00"
    val channelThumbnails = searchItem.channel?.thumbnails?.get(0)?.url ?: ""

    val showDialog = remember { mutableStateOf(false) }

    Card(
        onClick = { backStack.add(OnlineVideoPlayer(videoId = videoId)) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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

                IconButton(onClick = { showDialog.value = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showDialog.value) {
        ShowResultDialog(
            selectedItem = VideosListData(
                videoId, title, viewsNumber, dateOfVideo,
                duration, channelName, channelThumbnails
            ),
            onDismissRequest = {
                showDialog.value = false
            }
        )
    }
}

@Composable
private fun ShowResultDialog(
    selectedItem: VideosListData,
    onDismissRequest: () -> Unit
) {
    val mContext = LocalContext.current
    val thumbnailUrl = "https://img.youtube.com/vi/${selectedItem.videoId}/0.jpg"

    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (isLoading.value) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Fetching stream URL...")
                }
            }
        }
    }

    if (errorMessage.value != null) {
        AlertDialog(
            onDismissRequest = { errorMessage.value = null },
            confirmButton = {
                TextButton(onClick = { errorMessage.value = null }) {
                    Text("OK")
                }
            },
            title = { Text("Error") },
            text = { Text(errorMessage.value ?: "Unknown error") }
        )
    } else if (!isLoading.value && errorMessage.value == null) {
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Choose Action",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            isLoading.value = true

                            scope.launch {
                                selectedItem.loadStreamUrl(
                                    onSuccess = { streamResult ->
                                        isLoading.value = false

                                        if (streamResult.audioUrl.isBlank()) {
                                            errorMessage.value =
                                                "Audio stream URL is empty."
                                            return@loadStreamUrl
                                        }

                                        mContext.playAudioFromUrl(
                                            streamResult.audioUrl,
                                            selectedItem
                                        )

                                        onDismissRequest()
                                    },
                                    onFailure = {
                                        isLoading.value = false
                                        errorMessage.value =
                                            it.message ?: "Failed to fetch stream URL."
                                    }
                                )
                            }
                        }
                    ) {
                        Text("Play in Background")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            DownloadService.startForYouTube(
                                mContext,
                                selectedItem.videoId,
                                selectedItem.title,
                                DownloadType.MUSIC
                            )
                            onDismissRequest()
                        }
                    ) {
                        Text("Download Music")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            DownloadService.startForYouTube(
                                mContext,
                                selectedItem.videoId,
                                selectedItem.title,
                                DownloadType.VIDEO
                            )
                            onDismissRequest()
                        }
                    ) {
                        Text("Download Video")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
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
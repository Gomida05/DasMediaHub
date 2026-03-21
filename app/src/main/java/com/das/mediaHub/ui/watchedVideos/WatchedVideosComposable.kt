package com.das.mediaHub.ui.watchedVideos

import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import com.das.mediaHub.NavScreens.Saved
import com.das.mediaHub.NavScreens.VideoViewer
import com.das.mediaHub.OnLaunchComponents.playAudioFromUrl
import com.das.mediaHub.R
import com.das.mediaHub.data.local.WatchHistory
import com.das.mediaHub.data.model.SavedVideosListData
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import com.das.python.YouTuber.loadStreamUrl
import com.das.python.data.model.VideosListData
import com.das.python.data.model.searcher.Video
import kotlinx.coroutines.launch

@Composable
fun WatchedVideosComposable(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val dbHelper = remember {
        WatchHistory(context)
    }

    val viewModel = viewModel(
        modelClass = WatchedVideosViewModel::class.java.kotlin,
        factory = viewModelFactory {
            initializer {
                WatchedVideosViewModel(dbHelper)
            }
        }
    )
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val uiState by viewModel.savedListState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }
    val positionProvider = rememberTooltipPositionProvider(
        positioning = TooltipAnchorPosition.Below
    )
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
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    navigationIcon = {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TooltipBox(
                            modifier = Modifier,
                            positionProvider = positionProvider,
                            tooltip = {
                                PlainTooltip {
                                    Text(text = "Saved videos")
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = { backStack.add(Saved) }) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = "Saved videos",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            "Watch History",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (val newState = uiState) {
                    UiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is UiState.Error -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = newState.message,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    UiState.Empty -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(
                                            alpha = 0.3f
                                        ),
                                        modifier = Modifier.size(120.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = "Your history is empty",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Videos you watch will appear here.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    is UiState.Success -> {
                        items(newState.data, key = { it.watchUrl }) { searchItem ->
                            WatchedMediaItem(
                                backStack,
                                dbHelper = dbHelper,
                                selectedItem = searchItem,
                                viewModel = viewModel
                            )
                        }
                    }

                    else -> Unit

                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun WatchedMediaItem(
    backStack: NavBackStack<NavKey>,
    dbHelper: WatchHistory,
    selectedItem: SavedVideosListData,
    viewModel: WatchedVideosViewModel
) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val showInfoDialog = remember { mutableStateOf(false) }

    Card(
        onClick = {
            onClickListListener(
                dbHelper = dbHelper,
                selectedId = selectedItem.watchUrl,
                controller = backStack
            )
        },
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
                    model = selectedItem.thumbnailUrl,
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
                        text = selectedItem.duration,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = selectedItem.channelThumbnail,
                    error = painterResource(R.mipmap.ic_launcher_ofme),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { showInfoDialog.value = true },
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedItem.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${selectedItem.channelName} • ${selectedItem.viewer} • ${selectedItem.dateTime}",
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
        HistoryActionDialog(
            context = context,
            selectedData = selectedItem,
            deleteTheItem = {
                WatchHistory(context).deleteWatchUrl(selectedItem.watchUrl)
                viewModel.removeSearchItem(selectedItem)
            },
            onDismissRequest = { showDialog.value = false }
        )
    }
    if (showInfoDialog.value) {
        DevelopmentInfoDialog { showInfoDialog.value = false }
    }
}

@Composable
private fun DevelopmentInfoDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_ofme),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        },
        title = { Text("Under Development", fontWeight = FontWeight.Bold) },
        text = { Text("This feature is currently being built. Thank you for your patience! 😊") },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Okay") }
        }
    )
}

@Composable
private fun HistoryActionDialog(
    context: Context,
    selectedData: SavedVideosListData,
    deleteTheItem: (selectedId: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    val mediaDetails by retain {
        mutableStateOf(
            VideosListData(
                selectedData.watchUrl,
                selectedData.title,
                selectedData.viewer,
                selectedData.dateTime,
                selectedData.duration,
                selectedData.channelName,
                ""
            )
        )
    }

    val scope = rememberCoroutineScope()

    if (isLoading.value) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
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
            onDismissRequest = { errorMessage.value = null },
            title = { Text("Error") },
            text = { Text(errorMessage.value ?: "Unknown error") },
            confirmButton = {
                TextButton(onClick = { errorMessage.value = null }) {
                    Text("OK")
                }
            }
        )
    }

    if (!isLoading.value) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text("Manage History", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("What would you like to do with this video?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTheItem(selectedData.watchUrl)
                        onDismissRequest()
                    }
                ) {
                    Text(
                        "Remove from History",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isLoading.value = true

                        scope.launch {
                            mediaDetails.loadStreamUrl(
                                onSuccess = { streamResult ->
                                    isLoading.value = false

                                    if (streamResult.audioUrl.isBlank()) {
                                        errorMessage.value = "Audio stream URL is empty."
                                        return@loadStreamUrl
                                    }
                                    context.playAudioFromUrl(audioUrl = streamResult.audioUrl, streamResult)
                                    onDismissRequest()
                                },
                                onFailure = { throwable ->
                                    isLoading.value = false
                                    errorMessage.value =
                                        throwable.message ?: "Failed to fetch stream URL."
                                }
                            )
                        }
                    }
                ) {
                    Text("Play in Background")
                }
            }
        )
    }
}


private fun onClickListListener(
    dbHelper: WatchHistory,
    selectedId: String,
    controller: NavBackStack<NavKey>
) {
    try {
        val title = dbHelper.getVideoTitle(selectedId)
        controller.add(VideoViewer(Video(id = selectedId, title = title)))
    } catch (e: Exception) {
        showNotificationDialog = TopPopUp(
            message = "Error: ${e.message}",
            icon = Icons.Default.VideoLibrary,
            loading = false
        )
    }
}

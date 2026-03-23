package com.das.mediaHub.ui.watchedVideos

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import com.das.mediaHub.R
import com.das.mediaHub.data.local.WatchHistory
import com.das.mediaHub.data.model.SavedVideosListData
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.navigation.NavScreens.OnlineVideoPlayer
import com.das.mediaHub.navigation.NavScreens.Saved
import com.das.mediaHub.services.media.OnlineBackgroundPlayer.Companion.playAudioFromUrl
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import com.das.python.YouTuber.loadStreamUrl
import com.das.python.data.model.VideosListData
import com.das.python.data.model.searcher.Video
import kotlinx.coroutines.launch

@Composable
fun RecentlyWatchedVideosScreen(backStack: NavBackStack<NavKey>) {
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

@Composable
private fun WatchedMediaItem(
    backStack: NavBackStack<NavKey>,
    dbHelper: WatchHistory,
    selectedItem: SavedVideosListData,
    viewModel: WatchedVideosViewModel
) {
    val context = LocalContext.current
    val showMenu = remember { mutableStateOf(false) }
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

                Box {
                    IconButton(
                        onClick = { showMenu.value = true }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (showMenu.value) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                Color.Transparent
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = if (showMenu.value) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    HistoryActionMenu(
                        expanded = showMenu.value,
                        onDismissRequest = { showMenu.value = false },
                        context = context,
                        selectedData = selectedItem,
                        onRemoveFromHistory = {
                            dbHelper.deleteWatchUrl(selectedItem.watchUrl)
                            viewModel.removeSearchItem(selectedItem)
                        },
                        onOpenVideo = {
                            onClickListListener(
                                dbHelper = dbHelper,
                                selectedId = selectedItem.watchUrl,
                                controller = backStack
                            )
                        }
                    )
                }
            }
        }
    }

    if (showInfoDialog.value) {
        DevelopmentInfoDialog { showInfoDialog.value = false }
    }
}

@Composable
private fun DevelopmentInfoDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            shadowElevation = 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.size(78.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.mipmap.ic_launcher_ofme),
                            contentDescription = null,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Coming soon",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Channel details aren’t available yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "We’re still working on this feature.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Got it",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun HistoryActionMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    context: Context,
    selectedData: SavedVideosListData,
    onRemoveFromHistory: () -> Unit,
    onOpenVideo: () -> Unit
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
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Starting background play...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please wait a moment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    errorMessage.value?.let { message ->
        AlertDialog(
            onDismissRequest = { errorMessage.value = null },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Couldn't play video",
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorMessage.value = null }) {
                    Text("OK")
                }
            }
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(x = 0.dp, y = 8.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 12.dp,
        shadowElevation = 18.dp,
        modifier = Modifier
            .width(260.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(24.dp)
            )
            .border(
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = selectedData.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            ActionMenuItem(
                icon = Icons.Default.PlayArrow,
                title = "Open video",
                subtitle = "Watch now",
                onClick = {
                    onDismissRequest()
                    onOpenVideo()
                }
            )

            ActionMenuItem(
                icon = Icons.Default.Headphones,
                title = "Play in background",
                subtitle = "Audio only",
                onClick = {
                    isLoading.value = true
                    onDismissRequest()

                    scope.launch {
                        mediaDetails.loadStreamUrl(
                            onSuccess = { streamResult ->
                                isLoading.value = false

                                if (streamResult.audioUrl.isBlank()) {
                                    errorMessage.value =
                                        "This video can’t be played in the background right now."
                                    return@loadStreamUrl
                                }

                                context.playAudioFromUrl(
                                    audioUrl = streamResult.audioUrl,
                                    selectedItem = streamResult
                                )
                            },
                            onFailure = {
                                isLoading.value = false
                                errorMessage.value =
                                    "Couldn't start background play. Please try again."
                            }
                        )
                    }
                }
            )

            ActionMenuItem(
                icon = Icons.Default.Info,
                title = "Video details",
                subtitle = selectedData.channelName.ifBlank { "More info" },
                onClick = {
                    onDismissRequest()
                    showNotificationDialog = TopPopUp(
                        message = "More video details are coming soon.",
                        icon = Icons.Default.Info,
                        loading = false
                    )
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            ActionMenuItem(
                icon = Icons.Default.DeleteOutline,
                title = "Remove from history",
                subtitle = "Delete this item",
                titleColor = MaterialTheme.colorScheme.error,
                iconColor = MaterialTheme.colorScheme.error,
                onClick = {
                    onRemoveFromHistory()
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun ActionMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


private fun onClickListListener(
    dbHelper: WatchHistory,
    selectedId: String,
    controller: NavBackStack<NavKey>
) {
    try {
        val title = dbHelper.getVideoTitle(selectedId)
        controller.add(OnlineVideoPlayer(Video(id = selectedId, title = title)))
    } catch (e: Exception) {
        showNotificationDialog = TopPopUp(
            message = "Error: ${e.message}",
            icon = Icons.Default.VideoLibrary,
            loading = false
        )
    }
}

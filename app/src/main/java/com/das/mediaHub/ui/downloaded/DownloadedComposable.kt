package com.das.mediaHub.ui.downloaded

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.das.mediaHub.NavScreens
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.local.PathPreferences.audioPathState
import com.das.mediaHub.data.local.PathPreferences.videoPathState
import com.das.mediaHub.services.local.BackGroundPlayer
import com.das.mediaHub.ui.players.videoPlayer.state.UiState

@Composable
fun DownloadedComposable(backStack: NavBackStack<NavKey>, tabIndex: Int = 0) {
    val context = LocalContext.current
    val selectedTabIndex = remember { mutableIntStateOf(tabIndex) }
    val viewModel = viewModel(modelClass = DownloadedPageViewModel::class.java)
    val videoState by viewModel.videoUiState.collectAsStateWithLifecycle()
    val musicState by viewModel.musicUiState.collectAsStateWithLifecycle()


    val videoPath by videoPathState()
    val audioPath by audioPathState()
    val tabs = PageEnum.entries


    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.fetchVideoFiles(videoPath)
        viewModel.fetchMusicFiles(audioPath)
    }
    val currentState = if (selectedTabIndex.intValue == 0) videoState else musicState

    val isVideo = selectedTabIndex.intValue == 0
    val currentPath = if (isVideo) videoPath else audioPath


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
                CenterAlignedTopAppBar(
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    title = {
                        CustomTabRow(
                            selectedTabIndex = selectedTabIndex.intValue,
                            onTabSelected = { selectedTabIndex.intValue = it },
                            tabs = tabs
                        )
                    }
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (currentState) {
                    UiState.Idle,
                    UiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }

                    is UiState.Error -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentState.message,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(24.dp)
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
                                EmptyDownloadsState(isVideo = isVideo)
                            }
                        }
                    }

                    is UiState.Success -> {
                        val currentList = currentState.data

                        if (currentList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    EmptyDownloadsState(isVideo = isVideo)
                                }
                            }
                        } else {
                            item { Spacer(modifier = Modifier.height(8.dp)) }

                            items(
                                items = currentList,
                                key = { item -> item.mediaId }
                            ) { item ->

                                val index = currentList.indexOf(item)

                                DownloadItem(
                                    itemDetails = item,
                                    isVideo = isVideo,
                                    mContext = context,
                                    onClick = {
                                        itemClicked(
                                            index = index,
                                            selectedFilePath = item.mediaId,
                                            isVideo = isVideo,
                                            context = context,
                                            backStack = backStack
                                        )
                                    },
                                    onDelete = {
                                        viewModel.deleteFileAndRefresh(
                                            filePath = item.mediaId,
                                            isVideo = isVideo,
                                            pathLocation = currentPath
                                        )
                                    }
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadItem(
    itemDetails: MediaItem,
    isVideo: Boolean,
    mContext: Context,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val showAlertDialog = remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (isVideo) {
                    AsyncImage(
                        model = ImageRequest.Builder(mContext)
                            .data(itemDetails.mediaId.toUri())
                            .videoFrameMillis(10_000)
                            .decoderFactory { result, options, _ ->
                                VideoFrameDecoder(result.source, options)
                            }
                            .build(),
                        error = rememberVectorPainter(Icons.Default.Movie),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = itemDetails.mediaMetadata.title?.toString().orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isVideo) {
                        "${itemDetails.mediaMetadata.artist} • ${itemDetails.mediaMetadata.description}"
                    } else {
                        "${itemDetails.mediaMetadata.artist} • ${itemDetails.mediaMetadata.description}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { showAlertDialog.value = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showAlertDialog.value) {
        DeleteFileDialog(
            onDismiss = { showAlertDialog.value = false },
            onDelete = {
                onDelete()
                showAlertDialog.value = false
            }
        )
    }
}

@Composable
private fun EmptyDownloadsState(isVideo: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isVideo) Icons.Default.VideoLibrary else Icons.Default.LibraryMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isVideo) "No videos downloaded" else "No music found",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Your downloaded media will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun DeleteFileDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Delete File?", fontWeight = FontWeight.Bold) },
        text = { Text("Are you sure you want to permanently delete this file from your device?") },
        confirmButton = {
            TextButton(onClick = {
                onDelete()
                onDismiss()
            }) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun itemClicked(
    index: Int,
    selectedFilePath: String,
    isVideo: Boolean,
    context: Context,
    backStack: NavBackStack<NavKey>
) {
    if (isVideo) {
        backStack.add(NavScreens.ExoPlayerUI(selectedFilePath))
    } else {
        val playIntent = Intent(context, BackGroundPlayer::class.java).apply {
            action = ACTION_START
            putExtra("media_id", index)
        }
        ContextCompat.startForegroundService(context, playIntent)
    }
}

@Composable
private fun CustomTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<PageEnum>
) {
    val haptic = LocalHapticFeedback.current
    val tabWidths = remember { mutableStateMapOf<Int, Float>() }
    val density = LocalDensity.current
    val indicatorOffset by animateDpAsState(
        targetValue = tabWidths.entries.take(selectedTabIndex).sumOf { it.value.toDouble() }.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "indicatorOffset"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = tabWidths[selectedTabIndex]?.dp ?: 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "indicatorWidth"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .width(280.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        // Animated indicator
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(indicatorWidth)
                .fillMaxHeight()
                .padding(4.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                .zIndex(1f)
        )

        // Tab items
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, mode ->
                val isSelected = selectedTabIndex == index
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "textColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(index)
                        }
                        .onGloballyPositioned {
                            tabWidths[index] = it.size.width.toFloat() / density.density
                        }
                        .zIndex(2f),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (mode) {
                                PageEnum.VIDEOS -> Icons.Filled.VideoLibrary
                                PageEnum.AUDIOS -> Icons.Filled.LibraryMusic
                            },
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = mode.title,
                            color = textColor,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

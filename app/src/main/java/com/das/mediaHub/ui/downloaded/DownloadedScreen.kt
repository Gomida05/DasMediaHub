package com.das.mediaHub.ui.downloaded

import android.content.Context
import android.content.Intent
import android.util.Xml
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.local.Preferences.audioPathState
import com.das.mediaHub.data.local.Preferences.videoPathState
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.navigation.NavScreens
import com.das.mediaHub.services.media.LocalBackGroundPlayer
import java.io.File
import java.net.URLDecoder

@Composable
fun DownloadedScreen(
    backStack: NavBackStack<NavKey>,
    tabIndex: Int = 0
) {
    val context = LocalContext.current
    val selectedTabIndex = retain { mutableIntStateOf(tabIndex) }
    val viewModel = viewModel(modelClass = DownloadedPageViewModel::class.java)
    val videoState by viewModel.videoUiState.collectAsStateWithLifecycle()
    val musicState by viewModel.musicUiState.collectAsStateWithLifecycle()

    val videoPath by videoPathState()
    val audioPath by audioPathState()
    val tabs = PageEnum.entries

    LaunchedEffect(Unit) {
        viewModel.initialize(videoPath = videoPath, audioPath = audioPath)
    }

    val isVideo = selectedTabIndex.intValue == 0
    val currentState = if (isVideo) videoState else musicState
    val currentPath = if (isVideo) videoPath else audioPath

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
                        )
                    )
                ),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(13))
                ) {
                    DownloadedHeader(
                        isVideo = isVideo,
                        selectedTabIndex = selectedTabIndex.intValue,
                        onTabSelected = { selectedTabIndex.intValue = it },
                        tabs = tabs,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }

            when (currentState) {
                UiState.Idle,
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
                        MessageState(
                            title = "Couldn’t load media",
                            message = currentState.message,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                UiState.Empty -> {
                    item {
                        EmptyDownloadsState(isVideo = isVideo)
                    }
                }

                is UiState.Success -> {
                    val currentList = currentState.data

                    if (currentList.isEmpty()) {
                        item {
                            EmptyDownloadsState(isVideo = isVideo)
                        }
                    } else {

                        item {
                            DownloadedSummaryCard(
                                isVideo = isVideo,
                                count = currentList.size
                            )
                        }

                        itemsIndexed(
                            items = currentList,
                            key = { _, item -> item.mediaId }
                        ) { index, item ->
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
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadedHeader(
    isVideo: Boolean,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<PageEnum>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = 28.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(13)),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = if (isVideo) {
                        "Your downloaded videos"
                    } else {
                        "Your downloaded audio"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isVideo) Icons.Default.VideoLibrary else Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isVideo) "Videos" else "Audio",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        CustomTabRow(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected,
            tabs = tabs
        )
    }
}

@Composable
private fun DownloadedSummaryCard(
    isVideo: Boolean,
    count: Int
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = if (isVideo) Icons.Default.VideoLibrary else Icons.Default.LibraryMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp)
                )
            }

            Column {
                Text(
                    text = if (isVideo) "Downloaded videos" else "Downloaded audio",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (count == 1) {
                        "1 item available offline"
                    } else {
                        "$count items available offline"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DownloadItem(
    itemDetails: MediaItem,
    isVideo: Boolean,
    mContext: Context,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val showMenu = remember { mutableStateOf(false) }
    val showAlertDialog = remember { mutableStateOf(false) }
    val showInfoDialog = remember { mutableStateOf(false) }

    val imageLoader = retain(itemDetails.mediaId) {
        ImageLoader.Builder(mContext)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }

    val title = itemDetails.mediaMetadata.title?.toString().orEmpty().ifBlank { "Unknown title" }
    val secondary = itemDetails.mediaMetadata.description?.toString().orEmpty()
    val fileSize = itemDetails.mediaMetadata.artist?.toString().orEmpty()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 84.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                if (isVideo) {
                    AsyncImage(
                        model = itemDetails.mediaId.toUri(),
                        imageLoader = imageLoader,
                        error = rememberVectorPainter(Icons.Default.Movie),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.35f),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LibraryMetaChip(
                        icon = if (isVideo) Icons.Default.VideoLibrary else Icons.Default.LibraryMusic,
                        text = if (isVideo) "Video" else "Audio"
                    )

                    if (fileSize.isNotBlank()) {
                        LibraryMetaChip(
                            icon = Icons.Default.Info,
                            text = fileSize
                        )
                    }
                }

                if (secondary.isNotBlank()) {
                    Text(
                        text = secondary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box {
                FilledTonalIconButton(
                    onClick = { showMenu.value = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More actions",
                        modifier = Modifier.size(18.dp)
                    )
                }

                LocalMediaActionMenu(
                    expanded = showMenu.value,
                    onDismissRequest = { showMenu.value = false },
                    selectedData = itemDetails,
                    isVideo = isVideo,
                    onPlay = onClick,
                    onShowInfo = {
                        showInfoDialog.value = true
                    },
                    onOpenFolder = {
                        showInFileManager(
                            mContext,
                            itemDetails.mediaId
                        )
                    },
                    onRemoveDownload = {
                        showAlertDialog.value = true
                    }
                )
            }
        }
    }

    if (showInfoDialog.value) {
        LocalMediaInfoDialog(
            itemDetails = itemDetails,
            isVideo = isVideo,
            onDismiss = { showInfoDialog.value = false }
        )
    }

    if (showAlertDialog.value) {
        DeleteFileDialog(
            fileName = itemDetails.mediaMetadata.title?.toString().orEmpty(),
            onDismiss = { showAlertDialog.value = false },
            onDelete = {
                onDelete()
                showAlertDialog.value = false
            }
        )
    }
}


@Composable
private fun LibraryMetaChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyDownloadsState(
    isVideo: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isVideo) {
                            Icons.Default.VideoLibrary
                        } else {
                            Icons.Default.LibraryMusic
                        },
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = if (isVideo) "No videos downloaded" else "No audio downloaded",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Downloaded media will appear here for quick offline access.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
private fun DeleteFileDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = "Remove download?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (fileName.isNotBlank()) {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "This will delete the downloaded file from your device. You can’t undo this action.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text(
                    text = "Remove",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep")
            }
        }
    )
}

@Composable
private fun MessageState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
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
        targetValue = tabWidths.entries
            .sortedBy { it.key }
            .take(selectedTabIndex)
            .sumOf { it.value.toDouble() }.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 220f),
        label = "indicatorOffset"
    )

    val indicatorWidth by animateDpAsState(
        targetValue = tabWidths[selectedTabIndex]?.dp ?: 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 220f),
        label = "indicatorWidth"
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val controlWidth = maxWidth

        Box(
            modifier = Modifier
                .width(controlWidth)
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(indicatorWidth)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .shadow(2.dp, RoundedCornerShape(22.dp))
                    .zIndex(1f)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, mode ->
                    val isSelected = selectedTabIndex == index
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        },
                        animationSpec = spring(dampingRatio = 0.8f),
                        label = "tabTextColor"
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    PageEnum.VIDEOS -> Icons.Filled.VideoLibrary
                                    PageEnum.AUDIOS -> Icons.Filled.LibraryMusic
                                },
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
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
}

@Composable
private fun LocalMediaInfoDialog(
    itemDetails: MediaItem,
    isVideo: Boolean,
    onDismiss: () -> Unit
) {
    val title = itemDetails.mediaMetadata.title?.toString().orEmpty().ifBlank { "Unknown title" }
    val artist = itemDetails.mediaMetadata.artist?.toString().orEmpty()
    val description = itemDetails.mediaMetadata.description?.toString().orEmpty()

    val rawPath = itemDetails.mediaId
    val fileName = rawPath.toDisplayFileName()
    val folderPath = rawPath.toPrettyFolderPath()
    val mediaType = if (isVideo) "Video" else "Audio"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            shadowElevation = 18.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isVideo) Icons.Default.Movie else Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Media details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isVideo) "Local video file" else "Local audio file",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailChip(
                                icon = if (isVideo) Icons.Default.VideoLibrary else Icons.Default.LibraryMusic,
                                text = mediaType
                            )

                            if (artist.isNotBlank()) {
                                DetailChip(
                                    icon = Icons.Default.Person,
                                    text = artist
                                )
                            }
                        }
                    }
                }

                DetailSection(
                    label = "File name",
                    value = fileName,
                    icon = Icons.Default.Description
                )

                DetailSection(
                    label = "Location",
                    value = folderPath,
                    icon = Icons.Default.Folder
                )

                if (description.isNotBlank()) {
                    DetailSection(
                        label = "Description",
                        value = description,
                        icon = Icons.Default.Info
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Close",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailSection(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }
    }
}

private fun itemClicked(
    index: Int,
    selectedFilePath: String,
    isVideo: Boolean,
    context: Context,
    backStack: NavBackStack<NavKey>
) {
    if (isVideo) {
        backStack.add(NavScreens.LocalVideoPlayer(selectedFilePath))
    } else {
        val playIntent = Intent(context, LocalBackGroundPlayer::class.java).apply {
            action = ACTION_START
            putExtra("media_id", index)
        }
        ContextCompat.startForegroundService(context, playIntent)
    }
}

private fun showInFileManager(
    context: Context,
    rawPath: String
) {
    val normalizedPath = rawPath.toLocalFilePath() ?: return
    val file = File(normalizedPath)
    val target = if (file.isDirectory) file else file.parentFile ?: return

    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            target
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Open in file manager"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


private fun String.toLocalFilePath(): String? {
    return when {
        startsWith("file://") -> this.toUri().path
        startsWith("file:") -> removePrefix("file:")
        else -> this
    }
}

private fun String.toDisplayFolderPath(): String {
    val localPath = toLocalFilePath().orEmpty()
    if (localPath.isBlank()) return "Unknown location"

    val file = File(localPath)
    return file.parent ?: localPath
}

private fun String.toDisplayFileName(): String {
    val localPath = toLocalFilePath().orEmpty()
    if (localPath.isBlank()) return "Unknown file"

    return File(localPath)
        .name
        .cleanFileName()
        .ifBlank { "Unknown file" }
}

private fun String.cleanFileName(): String {
    return try {
        val decoded = URLDecoder.decode(this, Xml.Encoding.UTF_8.name)

        decoded
            .replace(Regex("\\s+"), " ") // fix double spaces
            .trim()
    } catch (_: Exception) {
        this
    }
}

private fun String.toPrettyFolderPath(): String {
    val folder = toDisplayFolderPath()
    return folder
        .replace("/storage/emulated/0", "Internal storage")

}
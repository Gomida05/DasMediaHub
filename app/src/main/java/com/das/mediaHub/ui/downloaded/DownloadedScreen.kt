package com.das.mediaHub.ui.downloaded

import android.content.Context
import android.content.Intent
import android.util.Xml
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.local.ThemePreferences.audioPathState
import com.das.mediaHub.data.local.ThemePreferences.videoPathState
import com.das.mediaHub.data.model.interfaces.UiState
import com.das.mediaHub.navigation.AppBackStack
import com.das.mediaHub.navigation.Destination
import com.das.mediaHub.services.media.local.LocalBackGroundPlayer
import java.io.File
import java.net.URLDecoder

/**
 * Downloaded media screen for displaying locally stored audio and video content.
 *
 * This screen provides a unified library-style interface where users can:
 * - Browse downloaded videos and audio via tab selection.
 * - View metadata such as title, description, artist, and file location.
 * - Play media using the integrated local player.
 * - Open files in an external file manager.
 * - View detailed media information.
 * - Delete downloaded files with confirmation.
 *
 * The UI reacts to state changes from [DownloadedPageViewModel] and supports:
 * - Loading, empty, success, and error states.
 * - Dynamic content switching between video and audio sources.
 *
 * Key features:
 * - Sticky header with navigation and tab controls.
 * - Lazy-loaded list of downloaded items.
 * - Media playback integration (foreground for video, background for audio).
 * - File system interaction via [FileProvider].
 * - Material 3 theming with adaptive components.
 *
 * @param backStack Navigation back stack used for handling screen transitions.
 * @param tabIndex Initial tab index (0 = videos, 1 = audio).
 *
 * @see DownloadedPageViewModel
 * @see LocalBackGroundPlayer
 * @see Destination
 */
@Composable
fun DownloadedScreen(
    backStack: AppBackStack,
    tabIndex: Int = 0
) {
    val context = LocalContext.current

    val selectedTabIndex = retain { mutableIntStateOf(tabIndex) }
    val viewModel = hiltViewModel<DownloadedPageViewModel>()
    val videoState by viewModel.videoUiState.collectAsStateWithLifecycle()
    val musicState by viewModel.musicUiState.collectAsStateWithLifecycle()

    val player = viewModel.justExoPlayer

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
        topBar = {
            DownloadedHeader(
                isVideo = isVideo,
                selectedTabIndex = selectedTabIndex.intValue,
                onTabSelected = { selectedTabIndex.intValue = it },
                tabs = tabs,
                onBack = { backStack.removeLastOrNull() }
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 340.dp), // Fits 1 item on phones, 2+ on tablets
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
            contentPadding = paddingValues,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (currentState) {
                UiState.Idle,
                UiState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        MessageState(
                            message = currentState.message,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                UiState.Empty -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyDownloadsState(isVideo = isVideo)
                    }
                }

                is UiState.Success -> {
                    val currentList = currentState.data

                    if (currentList.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyDownloadsState(isVideo = isVideo)
                        }
                    } else {
                        item(span = { GridItemSpan(maxLineSpan) }) {
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
                                context = context,
                                itemDetails = item,
                                isVideo = isVideo,
                                onClick = {
                                    itemClicked(
                                        player = player,
                                        index = index,
                                        currentList = currentList,
                                        selectedItem = item.mediaId,
                                        isVideo = isVideo,
                                        context = context,
                                        backStack = backStack
                                    )
                                },
                                onDelete = {
                                    viewModel.deleteFileAndRefresh(
                                        filePath = item.mediaId.toLocalFilePath() ?: item.mediaId,
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
    Card(
        shape = MaterialTheme.shapes.extraLarge.copy(
            topStart = CornerSize(0.dp),
            topEnd = CornerSize(0.dp)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(top = 24.dp, bottom = 12.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderTopRow(
                isVideo = isVideo,
                onBack = onBack
            )
        }

        SimpleTabRow(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected,
            tabs = tabs
        )
    }
}

@Composable
private fun HeaderTopRow(
    isVideo: Boolean,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isVideo) "Your downloaded videos" else "Your downloaded audio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        MediaTypeChip(isVideo = isVideo)
    }
}

@Composable
private fun MediaTypeChip(isVideo: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isVideo) Icons.Default.VideoLibrary else Icons.Default.LibraryMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isVideo) "Videos" else "Audio",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SimpleTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<PageEnum>
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedTabIndex

            val background by animateColorAsState(
                if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else
                    Color.Transparent,
                label = "tabBackground"
            )

            val contentColor by animateColorAsState(
                if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                label = "tabColor"
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(background)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTabSelected(index)
                    }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (tab) {
                        PageEnum.VIDEOS -> Icons.Default.VideoLibrary
                        PageEnum.AUDIOS -> Icons.Default.LibraryMusic
                    },
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = tab.title,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
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
    context: Context,
    itemDetails: MediaItem,
    isVideo: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val imageLoaderLocal = retain {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }


    var showMenu by retain { mutableStateOf(false) }
    var showDelete by retain { mutableStateOf(false) }
    var showInfo by retain { mutableStateOf(false) }

    val title = itemDetails.mediaMetadata.title?.toString().orEmpty().ifBlank { "Unknown title" }
    val description = itemDetails.mediaMetadata.description?.toString().orEmpty()
    val meta = itemDetails.mediaMetadata.artist?.toString().orEmpty()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            DownloadThumbnail(
                isVideo = isVideo,
                mediaId = itemDetails.mediaId,
                imageLoaderLocal = imageLoaderLocal
            )

            Spacer(modifier = Modifier.width(12.dp))

            DownloadInfo(
                title = title,
                description = description,
                meta = meta,
                isVideo = isVideo
            )

            DownloadActions(
                onOpenMenu = { showMenu = true }
            )

            DownloadMenu(
                expanded = showMenu,
                onDismiss = { showMenu = false },
                item = itemDetails,
                isVideo = isVideo,
                onPlay = onClick,
                onInfo = { showInfo = true },
                onDelete = { showDelete = true },
                onOpenFolder = {
                    showInFileManager(context, itemDetails.mediaId)
                }
            )
        }
    }

    if (showInfo) {
        LocalMediaInfoDialog(
            itemDetails = itemDetails,
            isVideo = isVideo,
            onDismiss = {
                showInfo = false
            }
        )
    }

    if (showDelete) {
        DeleteFileDialog(
            fileName = title
        ) {
            showDelete = false
            it?.let {
                onDelete()
            }
        }
    }
}


@Composable
private fun DownloadThumbnail(
    isVideo: Boolean,
    mediaId: String,
    imageLoaderLocal: ImageLoader
) {


    Box(
        modifier = Modifier
            .size(width = 92.dp, height = 80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        if (isVideo) {
            AsyncImage(
                model = mediaId.toUri(),
                imageLoader = imageLoaderLocal,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .padding(6.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun RowScope.DownloadInfo(
    title: String,
    description: String,
    meta: String,
    isVideo: Boolean
) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LibraryMetaChip(
                icon = if (isVideo) Icons.Default.VideoLibrary else Icons.Default.LibraryMusic,
                text = if (isVideo) "Video" else "Audio"
            )

            if (meta.isNotBlank()) {
                LibraryMetaChip(
                    icon = Icons.Default.Info,
                    text = meta
                )
            }
        }

        if (description.isNotBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
private fun DownloadActions(
    onOpenMenu: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onOpenMenu,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}



@Composable
private fun DownloadMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    item: MediaItem,
    isVideo: Boolean,
    onPlay: () -> Unit,
    onInfo: () -> Unit,
    onDelete: () -> Unit,
    onOpenFolder: () -> Unit
) {
    LocalMediaActionMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        selectedData = item,
        isVideo = isVideo,
        onPlay = onPlay,
        onShowInfo = onInfo,
        onOpenFolder = onOpenFolder,
        onRemoveDownload = onDelete
    )
}

@Composable
private fun LibraryMetaChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
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

/**
 * Displays a confirmation dialog for deleting a downloaded file.
 *
 * This dialog presents the user with the selected file name (if available)
 * and warns that the deletion action is permanent and cannot be undone.
 *
 * @param fileName The name of the file to be displayed in the dialog.
 * If blank, the file name section will be omitted.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * - Returns `true` if the user confirms deletion.
 * - Returns `null` if the dialog is dismissed or the user cancels the action.
 */
@Composable
private fun DeleteFileDialog(
    fileName: String,
    onDismiss: (Boolean?) -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss(null)
        },
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
            TextButton(
                onClick = {
                    onDismiss(true)
                }
            ) {
                Text(
                    text = "Remove",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss(null)
                }
            ) {
                Text("Keep")
            }
        }
    )
}

@Composable
private fun MessageState(
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
                text = "Couldn’t load media",
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
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
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
    player: Player,
    index: Int,
    currentList: List<MediaItem>,
    isVideo: Boolean,
    context: Context,
    backStack: AppBackStack,
    selectedItem: String
) {
    if (isVideo) {
        backStack.add(Destination.LocalVideoPlayer(selectedItem))
    } else {


        val isSamePlaylist = player.mediaItemCount == currentList.size &&
                player.currentMediaItemIndex in currentList.indices &&
                currentList.indices.all { i ->
                    player.getMediaItemAt(i).mediaId == currentList[i].mediaId
                }
        if (isSamePlaylist) {
            // Only move to the selected item
            if (player.currentMediaItemIndex != index) {
                player.seekTo(index, 0)
            }
            if (!player.isPlaying) player.play()
        } else {
            // New playlist → set once
            player.setMediaItems(currentList, index, 0)
            player.prepare()
            val playIntent = Intent(context, LocalBackGroundPlayer::class.java).apply {
                action = ACTION_START
                putExtra("media_id", index)
            }
            context.startService(playIntent)
        }
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
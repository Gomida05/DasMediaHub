package com.das.mediaHub.ui.players.videoPlayer.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.More
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.das.downloader.data.model.download.DownloadType
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.data.model.interfaces.VideoAction
import com.das.mediaHub.data.model.icons.filled.YouTubeIcon
import com.das.mediaHub.data.model.interfaces.UiState
import com.das.mediaHub.ui.components.ErrorStateView
import com.das.mediaHub.ui.components.dialogs.ActionMenuItem
import com.das.mediaHub.ui.notification.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.players.videoPlayer.components.CustomLayouts.SkeletonLoadingLayout
import com.das.python.YouTuber.formatDate
import com.das.python.data.model.FewVideoDetails

@Composable
internal fun VideoDetailsComposable(
    videoId: String,
    isSaved: Boolean,
    channelThumbnailURL: String,
    detailsState: UiState<FewVideoDetails>,
    onVideoAction: (VideoAction) -> Unit
) {
    val showMenu = retain { mutableStateOf(false) }

    when (detailsState) {
        UiState.Idle,
        UiState.Loading -> {
            // Added a wrapper to ensure consistent padding with the success state
            Box(modifier = Modifier.padding(16.dp)) {
                SkeletonLoadingLayout()
            }
        }

        UiState.Empty -> {
            ErrorStateView(
                title = "No Details Found",
                message = "We couldn't fetch the information for this video.",
                icon = Icons.Rounded.SearchOff
            )
        }

        is UiState.Error -> {
            ErrorStateView(
                title = "Oops! Something went wrong",
                message = detailsState.message,
                icon = Icons.Rounded.WarningAmber
            )
        }

        is UiState.Success -> {
            val videoDetails = detailsState.data
            val title = videoDetails.title

            LaunchedEffect(channelThumbnailURL) {
                if (channelThumbnailURL != "none is here") {
                    onVideoAction(VideoAction.ToggleHistory)
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow, // Standard M3 container color
                shape = RoundedCornerShape(24.dp), // Smoother, more modern corner radius
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // --- Title Section ---
                    Text(
                        text = videoDetails.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Channel Info Section ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Channel Avatar
                        if (channelThumbnailURL == "none is here") {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            AsyncImage(
                                model = channelThumbnailURL,
                                error = rememberVectorPainter(Icons.Default.Error),
                                contentDescription = "Channel Thumbnail",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Channel Metadata
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = videoDetails.channelName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = videoDetails.viewNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )

                                // Bullet separator for a cleaner look
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = videoDetails.date.formatDate(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        item {
                            ActionIconButton(
                                icon = if (isSaved) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                                label = if (isSaved) "Saved" else "Save",
                                tint = if (isSaved) Color.Red else MaterialTheme.colorScheme.onSurface
                            ) {
                                onVideoAction(
                                    VideoAction.ToggleFavorite(insert = !isSaved)
                                )
                            }
                        }



                        item {
                            ActionIconButton(
                                icon = Icons.Default.MusicNote,
                                label = "MP3"
                            ) {
                                onVideoAction(
                                    VideoAction.Download(
                                    id = videoId,
                                    title = title,
                                    type = DownloadType.YOUTUBE_AUDIO
                                )
                                )
                            }
                        }

                        item {
                            ActionIconButton(
                                icon = Icons.Default.Videocam,
                                label = "MP4"
                            ) {
                                onVideoAction(
                                    VideoAction.Download(
                                        id = videoId,
                                        title = title,
                                        type = DownloadType.YOUTUBE_VIDEO
                                    )
                                )
                            }
                        }


                        item {
                            ActionIconButton(
                                icon = Icons.Default.Share,
                                label = "Share"
                            ) {
                               onVideoAction(VideoAction.Share)
                            }
                        }

                        item {
                            Box {
                                ActionIconButton(
                                    icon = Icons.AutoMirrored.Default.More,
                                    label = "More"
                                ) {
                                    showMenu.value = !showMenu.value
                                }
                                VideoActionMenu(
                                    title = title,
                                    expanded = showMenu.value,
                                    onDismissRequest = { showMenu.value = false },
                                ) { _, _ ->
                                    VideoDetailsActionItems(
                                        channelName = "",
                                        onVideoAction = onVideoAction,
                                        onDismissRequest = {
                                            showMenu.value = false
                                        }
                                    )
                                }
                            }
                        }

                    }
                }
            }

        }
    }
}

@Composable
fun VideoDetailsActionItems(
    channelName: String,
    onVideoAction: (VideoAction) -> Unit,
    onDismissRequest: () -> Unit,
) {


    ActionMenuItem(
        icon = Icons.Default.YouTubeIcon,
        title = "Open in YouTube",
        subtitle = "Watch in official app",
        iconColor = Color.Red,
        onClick = {
            onVideoAction(VideoAction.PlayInYoutube)
            onDismissRequest()
        }
    )

    // 🎧 Background play
    ActionMenuItem(
        icon = Icons.Default.Headphones,
        title = "Play in background",
        subtitle = "Audio only",
        onClick = {
            onVideoAction(VideoAction.PlayBackground)
            onDismissRequest()
        }
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
fun VideoActionMenu(
    title: String,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    addActionMenuItems: @Composable ( MutableState<Boolean>,  MutableState<String?>) -> Unit
) {
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }





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
        Column(modifier = Modifier.padding(vertical = 8.dp)) {

            // Header
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
                    text = title,
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

            addActionMenuItems(
                isLoading,
                errorMessage
            )
        }
    }
}

@Composable
private fun ActionIconButton(
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



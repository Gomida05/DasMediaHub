package com.das.mediaHub.ui.components.dialogs

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.data.model.VideoItem
import com.das.mediaHub.services.media.OnlineBackgroundPlayer.Companion.playAudioFromUrl
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.downloaded.ActionMenuItem
import com.das.python.YouTuber.loadStreamUrl
import com.das.python.data.model.VideosListData
import kotlinx.coroutines.launch

@Composable
fun LibraryVideoActionMenu(
    context: Context,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    selectedData: VideoItem,
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
                selectedData.views,
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
fun ActionMenuItem(
    icon: ImageVector,
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
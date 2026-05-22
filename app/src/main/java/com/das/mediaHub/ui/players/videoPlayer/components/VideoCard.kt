package com.das.mediaHub.ui.players.videoPlayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.das.mediaHub.data.model.VideoAction
import com.das.mediaHub.ui.result.DownloadTypeDialog
import com.das.mediaHub.ui.result.ResultScreenActionItems
import com.das.python.data.model.searcher.Video

@Composable
fun VideoCard(
    searchItem: Video,
    onPlayThis: () -> Unit,
    onVideoAction: (VideoAction) -> Unit,
) {
    val videoId = searchItem.id
    val title = searchItem.title ?: ""
    val viewsNumber = searchItem.viewCount?.short ?: "0"
    val dateOfVideo = searchItem.publishedTime ?: ""
    val channelName = searchItem.channel?.name ?: ""
    val duration = searchItem.duration ?: "0:00"

    val showMenu = remember { mutableStateOf(false) }
    val showDownloadType = remember { mutableStateOf(false) }

    Card(
        onClick = onPlayThis,
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
            Box {
                IconButton(onClick = { showMenu.value = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(20.dp))
                }

                VideoActionMenu(
                    title = title,
                    expanded = showMenu.value,
                    onDismissRequest = { showMenu.value = false }
                ) { _, _ ->

                    ResultScreenActionItems(
                        channelName = channelName,
                        title = title,
                        playItHere = {
                            onPlayThis()
                        },
                        playItInYouTube = {
                            onVideoAction(VideoAction.PlayInYoutube)
                        },
                        showDownloadType = {
                            showDownloadType.value = true
                        },
                        playItInBackground = {
                            onVideoAction(VideoAction.PlayBackground)
                        }
                    ) { showMenu.value = false }
                }
            }
        }
    }

    if (showDownloadType.value) {
        DownloadTypeDialog(
            mediaTitle = title,
            onDismiss = { type->
                showDownloadType.value = false
                type?.let {
                    onVideoAction(
                        VideoAction.Download(
                            id = videoId,
                            title = title,
                            it
                        )
                    )
                }
            }
        )
    }
}

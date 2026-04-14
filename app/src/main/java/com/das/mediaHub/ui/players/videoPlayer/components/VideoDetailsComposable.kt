package com.das.mediaHub.ui.players.videoPlayer.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.More
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.das.downloader.data.model.download.DownloadType
import com.das.mediaHub.MainApplication
import com.das.mediaHub.data.model.icons.filled.YouTubeIcon
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.data.repository.FavoritesRepository
import com.das.mediaHub.data.repository.WatchHistoryRepository
import com.das.mediaHub.services.download.DownloadService
import com.das.mediaHub.ui.players.videoPlayer.ViewerViewModel
import com.das.mediaHub.ui.players.videoPlayer.components.CustomLayouts.SkeletonLoadingLayout
import com.das.python.YouTuber.formatDate
import kotlinx.coroutines.launch


@Composable
fun VideoDetailsComposable(
    mContext: Context,
    videoId: String,
    channelThumbnailURL: String,
    duration: String,
    viewModel: ViewerViewModel,
    clickForMore: () -> Unit,
    playItInYouTube: () -> Unit
) {
    val showDescriptionDialog = remember { mutableStateOf(false) }
    val detailsState by viewModel.detailsState.collectAsStateWithLifecycle()

    val app = mContext.applicationContext as MainApplication
    val appDatabase = retain {
        app.appDatabase
    }

    val dbForFav = retain {
        FavoritesRepository(appDatabase.favoritesDatabase.favoritesDao())
    }
    val watchHistory = retain { WatchHistoryRepository(appDatabase.historyDatabase.watchHistoryDao()) }
    val scope = rememberCoroutineScope()

    val isSaved by dbForFav.isWatchUrlExist(videoId).collectAsStateWithLifecycle(false)



    when (val state = detailsState) {
        UiState.Idle,
        UiState.Loading -> {
            SkeletonLoadingLayout()
        }

        UiState.Empty -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No video details found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        is UiState.Success -> {
            val videoDetails = state.data
            val title = videoDetails.title

            LaunchedEffect(channelThumbnailURL) {
                if (channelThumbnailURL != "none is here") {
                    scope.launch {
                        watchHistory.insertNewVideo(
                            videoId,
                            title,
                            videoDetails.date,
                            videoDetails.viewNumber,
                            videoDetails.channelName,
                            duration,
                            channelThumbnailURL
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = videoDetails.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    ),
                    maxLines = 3,
                    modifier = Modifier.clickable { showDescriptionDialog.value = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = channelThumbnailURL,
                        error = rememberVectorPainter(Icons.Default.Error),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = videoDetails.channelName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${videoDetails.viewNumber} • ${videoDetails.date.formatDate()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ActionIconButton(
                        icon = if (isSaved) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        label = if (isSaved) "Saved" else "Save",
                        tint = if (isSaved) Color.Red else MaterialTheme.colorScheme.onSurface
                    ) {
                        if (isSaved) {
                            scope.launch {
                                dbForFav.deleteWatchUrl(videoId)
                            }
                        } else {
                            scope.launch {
                                dbForFav.insertData(
                                    videoId,
                                    title,
                                    videoDetails.date,
                                    videoDetails.viewNumber,
                                    videoDetails.channelName,
                                    duration,
                                    channelThumbnailURL
                                )
                            }
                        }
                    }

                    ActionIconButton(
                        icon = Icons.Default.Share,
                        label = "Share"
                    ) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "https://youtu.be/$videoId")
                        }
                        mContext.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }

                    ActionIconButton(
                        icon = Icons.Default.MusicNote,
                        label = "MP3"
                    ) {
                        DownloadService.startForYouTube(
                            context = mContext,
                            id = videoId,
                            title = title,
                            type = DownloadType.MUSIC
                        )
                    }

                    ActionIconButton(
                        icon = Icons.Default.Videocam,
                        label = "MP4"
                    ) {
                        DownloadService.startForYouTube(
                            context = mContext,
                            id = videoId,
                            title = title,
                            type = DownloadType.VIDEO
                        )
                    }

                    ActionIconButton(
                        icon = Icons.Default.YouTubeIcon,
                        label = "YouTube",
                        tint = Color.Red
                    ) {
                        playItInYouTube()
                    }

                    ActionIconButton(
                        icon = Icons.AutoMirrored.Default.More,
                        label = "More"
                    ) {
                        clickForMore()
                    }
                }
            }

            if (showDescriptionDialog.value) {
                ShowDescriptionDialog(videoDetails.description) {
                    showDescriptionDialog.value = false
                }
            }
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
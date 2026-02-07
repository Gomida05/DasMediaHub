package com.das.mediaHub.ui.watchedVideos

import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import com.das.mediaHub.R
import com.das.mediaHub.data.local.WatchHistory
import com.das.mediaHub.data.model.SavedVideosListData
import com.das.mediaHub.data.model.VideosListData
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.services.AudioServiceFromUrl
import com.das.mediaHub.NavScreens.VideoViewer
import com.das.mediaHub.NavScreens.Saved
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.data.model.searcher.Video
import com.das.mediaHub.python.YouTuber.loadStreamUrl
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog

@Composable
fun WatchedVideosComposable(backStack: NavBackStack<NavKey>) {
    val viewModel = viewModel(WatchedVideosViewModel::class.java.kotlin)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val searchResults by viewModel.savedLists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isError by viewModel.isError.collectAsState()
    val dbHelper = viewModel.dbHelper

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

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
                        IconButton(onClick = { backStack.add(Saved) }) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "Saved videos",
                                tint = MaterialTheme.colorScheme.primary
                            )
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (!isError.isNullOrEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = isError.toString(),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
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
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Videos you watch will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = paddingValues,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(searchResults, key = { it.watchUrl }) { searchItem ->
                        WatchedMediaItem(
                            backStack,
                            dbHelper = dbHelper,
                            selectedItem = searchItem,
                            viewModel = viewModel
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
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
    val shouldLoad = remember { mutableStateOf(false) }

    if (shouldLoad.value) {
        LaunchedEffect(Unit) {
            VideosListData(
                selectedData.watchUrl, selectedData.title, selectedData.viewer,
                selectedData.dateTime, selectedData.duration, selectedData.channelName, ""
            ).loadStreamUrl(
                onSuccess = {
                    val playIntent = Intent(context, AudioServiceFromUrl::class.java).apply {
                        action = ACTION_START
                        putExtra("videoId", selectedData.watchUrl)
                        putExtra("media_url", it.audioUrl)
                        putExtra("title", selectedData.title)
                        putExtra("channelName", selectedData.channelName)
                        putExtra("viewNumber", selectedData.viewer)
                        putExtra("videoDate", selectedData.dateTime)
                        putExtra("duration", selectedData.duration)
                    }
                    ContextCompat.startForegroundService(context, playIntent)
                },
                onFailure = { println("Error: $it") }
            )
            shouldLoad.value = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Manage History", fontWeight = FontWeight.Bold) },
        text = { Text("What would you like to do with this video?") },
        confirmButton = {
            TextButton(
                onClick = {
                    deleteTheItem(selectedData.watchUrl)
                    onDismissRequest()
                }
            ) {
                Text("Remove from History", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    shouldLoad.value = true
                    onDismissRequest()
                }
            ) {
                Text("Play in Background")
            }
        }
    )
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

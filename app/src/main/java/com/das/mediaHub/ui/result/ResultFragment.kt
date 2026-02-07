package com.das.mediaHub.ui.result

import android.content.Intent
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import com.das.mediaHub.MainActivity
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.services.AudioServiceFromUrl
import com.das.mediaHub.data.model.VideosListData
import com.das.mediaHub.NavScreens.VideoViewer
import com.das.mediaHub.python.YouTuber.loadStreamUrl
import com.das.mediaHub.data.model.searcher.Video
import com.das.mediaHub.ui.players.videoPlayer.CustomMethods.SkeletonSuggestionLoadingLayout
import kotlinx.coroutines.launch

@Composable
fun ResultViewerPage(backStack: NavBackStack<NavKey>, data: String) {

    val viewModel = viewModel(modelClass = ResultViewModel::class.java, key = "ResultViewModel_$data")
    val isLoading by viewModel.isLoading.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val foundError by viewModel.error.collectAsState()

    val isLoadingMore by viewModel.isLoadingMore.collectAsState()


    val snackBar = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()


    LaunchedEffect(data) {
        viewModel.fetchSuggestionsIfNeeded(data)
    }


    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

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
            snackbarHost = {
                SnackbarHost(snackBar)
            },
            containerColor = Color.Transparent,
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    title = {},
                    actions = {
                        Surface(
                            onClick = { backStack.removeLastOrNull() },
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = data,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        )
        { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            SkeletonSuggestionLoadingLayout(true)
                        }
                    }
                } else {
                    when {
                        searchResults.isEmpty() -> item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = foundError ?: "No results found for \"$data\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        !foundError.isNullOrEmpty() -> item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = foundError ?: "Something went wrong, please check your internet and try again!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        else -> {
                            itemsIndexed(items = searchResults) { index, item ->
                                if (index >= searchResults.size - 3 && !isLoadingMore) {
                                    viewModel.loadMore()
                                }
                                VideoResultItem(
                                    backStack,
                                    item
                                ) {
                                    scope.launch {
                                        snackBar.showSnackbar(it)
                                    }
                                }
                            }

                            if (isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
                                    }
                                }
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
fun VideoResultItem(
    backStack: NavBackStack<NavKey>,
    searchItem: Video,
    snackBar: (String) -> Unit
) {
    val videoId = searchItem.id
    val title = searchItem.title ?: ""
    val viewsNumber = searchItem.viewCount?.short ?: "0"
    val dateOfVideo = searchItem.publishedTime ?: ""
    val channelName = searchItem.channel?.name ?: ""
    val duration = searchItem.duration ?: "0:00"
    val channelThumbnails = searchItem.channel?.thumbnails?.get(0)?.url ?: ""

    val showDialog = remember { mutableStateOf(false) }

    Card(
        onClick = { backStack.add(VideoViewer(searchItem)) },
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
                    model = "https://img.youtube.com/vi/$videoId/0.jpg",
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
                        text = duration,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = channelThumbnails,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { snackBar(channelName) },
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$channelName • $viewsNumber • $dateOfVideo",
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
        ShowResultDialog(
            selectedItem = VideosListData(
                videoId, title, viewsNumber, dateOfVideo,
                duration, channelName, channelThumbnails
            ),
            onDismissRequest = {
                showDialog.value = false
            }
        )
    }
}

@Composable
private fun ShowResultDialog(
    selectedItem: VideosListData,
    onDismissRequest: () -> Unit
) {
    val mContext = LocalContext.current
    val thumbnailUrl = "https://img.youtube.com/vi/${selectedItem.videoId}/0.jpg"
    val shouldLoad = remember { mutableStateOf(false) }

    if (shouldLoad.value) {
        LaunchedEffect(shouldLoad.value) {
            selectedItem.loadStreamUrl(
                onSuccess = {
                    val playIntent = Intent(mContext, AudioServiceFromUrl::class.java).apply {
                        action = ACTION_START
                        putExtra("videoId", selectedItem.videoId)
                        putExtra("media_url", it.audioUrl)
                        putExtra("title", selectedItem.title)
                        putExtra("channelName", selectedItem.channelName)
                        putExtra("viewNumber", selectedItem.views)
                        putExtra("videoDate", selectedItem.dateOfVideo)
                        putExtra("duration", selectedItem.duration)
                    }
                    ContextCompat.startForegroundService(mContext, playIntent)
                },
                onFailure = {
                    println("Error: $it")
                }
            )
            shouldLoad.value = false
        }
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Action",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            shouldLoad.value = true
                            onDismissRequest()
                        }
                    ) {
                        Text(
                            text = "Background",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            (mContext as? MainActivity)?.startDownloadingAudio(
                                selectedItem.videoId,
                                selectedItem.title
                            )
                            onDismissRequest()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Music",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            (mContext as? MainActivity)?.startDownloadingVideo(
                                selectedItem.videoId,
                                selectedItem.title
                            )
                            onDismissRequest()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Video",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

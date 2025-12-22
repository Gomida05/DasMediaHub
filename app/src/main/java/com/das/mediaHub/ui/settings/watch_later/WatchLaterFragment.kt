package com.das.mediaHub.ui.settings.watch_later

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.das.mediaHub.MainActivity
import com.das.mediaHub.NavScreens
import com.das.mediaHub.R
import com.das.mediaHub.python.YouTuber.loadStreamUrl
import com.das.mediaHub.data.local.DatabaseFavorite
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.model.SavedVideosListData
import com.das.mediaHub.data.model.VideosListData
import com.das.mediaHub.services.AudioServiceFromUrl
import com.das.mediaHub.data.model.searcher.Video


@Composable
fun WatchLaterComposable(backStack: NavBackStack<NavKey>) {


    val viewModel = viewModel<WatchLaterViewModel>()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val videos by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        "List of videos",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->

        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
        ) {

            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                errorMessage != null -> {
                    item {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                videos.isEmpty() -> {
                    item {
                        Text(
                            text = "You don't have any saved videos yet.\nSave some videos to see them here!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }

                else -> {
                    items(videos, key = { it.watchUrl }) { video ->
                        WatchLaterItem(
                            backStack = backStack,
                            item = video,
                            viewModel = viewModel
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun WatchLaterItem(
    backStack: NavBackStack<NavKey>,
    item: SavedVideosListData,
    viewModel: WatchLaterViewModel
) {

    val showDialog = remember { mutableStateOf(false) }

    val showInfoDialog = remember { mutableStateOf(false) }

    val videoId = item.watchUrl
    val title = item.title
    val viewsNumber = item.viewer
    val dateOfVideo = item.dateTime
    val channelName = item.channelName
    val duration = item.duration
    val videoThumbnailURL = item.thumbnailUrl
    val channelThumbnails = item.channelThumbnail

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(1))
            .padding(bottom = 3.dp, top = 3.dp)
            .combinedClickable(
                onClick = {
                    onClickListListener(
                        context,
                        videoId,
                        backStack
                    )
                },
                onLongClick = {
                    showDialog.value = true
                }
            )
    ) {
        Column(
            modifier = Modifier
                .height(260.dp)
                .fillMaxWidth()

        ) {
            Box {
                AsyncImage(
                    model = videoThumbnailURL,
                    contentDescription = "Category Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = duration,
                    maxLines = 1,
                    color = Color.White,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier
                        .height(25.dp)
                        .padding(end = 6.dp, bottom = 3.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xCC2C2B2B), RoundedCornerShape(25))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {


                IconButton(
                    onClick = {
                        showInfoDialog.value = true
                    }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(channelThumbnails)
                            .crossfade(true)
                            .error(
                                R.mipmap.under_development
                            )
                            .build(),
                        contentDescription = "Category Image",
                        modifier = Modifier
                            .fillMaxSize(),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier
                        .width(285.dp)
                        .padding(3.dp)
                ) {


                    Text(
                        text = title,
                        maxLines = 1,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp)
                    )
                    Row {
                        Text(
                            text = channelName,
                            maxLines = 1,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .width(112.dp)
                                .padding(start = 2.dp)
                        )
                        Text(
                            text = viewsNumber,
                            maxLines = 1,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .width(55.dp)
                                .padding(start = 5.dp, end = 5.dp)
                        )
                        Text(
                            text = dateOfVideo,
                            maxLines = 1,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .width(100.dp)
                                .padding(start = 2.dp)
                        )
                    }

                }
                IconButton(
                    onClick = {
                        showDialog.value = true
                    }

                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.MoreVert),
                        contentDescription = "Back"
                    )
                }
            }
        }
    }
    if (showDialog.value){
        ShowAlertDialog(
            context,
            item,
            deleteTheItem = { selectedId->
                DatabaseFavorite(context).deleteWatchUrl(selectedId)
                viewModel.removeSearchItem(item)
            },
            onDismissRequest = {showDialog.value = false}
        )
    }
    if (showInfoDialog.value){
        InfoDialog{
            showInfoDialog.value = false
        }
    }
}


@Composable
private fun InfoDialog(onDismissRequest: () -> Unit){

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.mipmap.under_development),
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text ="This feature is currently under development!!!",
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Text(
                "Thank you!😊",
                fontSize = 18.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Okay")
            }
        },

    )

}

private fun onClickListListener(
        context: Context,
        selectedId: String,
        backStack: NavBackStack<NavKey>
    ) {
    try {
        val dbHelper = DatabaseFavorite(context)
        val viewNumber = dbHelper.getViewNumber(selectedId)
        val datVideo = dbHelper.getVideoDate(selectedId)
        val videoChannel = dbHelper.getVideoChannelName(selectedId)
        val ourDuration = dbHelper.getDuration(selectedId).toString()
        val title = dbHelper.getVideoTitle(selectedId)
        val channelThumbnail = dbHelper.getChannelNameThumbnail(selectedId)
        val bundle = Video(
            id = selectedId,
            title = title
        )

        backStack.add(NavScreens.VideoViewer(bundle))

    } catch (e: Exception) {
        MainActivity().alertUserError(context, e.message.toString())
    }
}

@Composable
private fun ShowAlertDialog(
    context: Context,
    selectedData: SavedVideosListData,
    deleteTheItem: (selectedId: String) -> Unit,
    onDismissRequest: () ->Unit
){

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
                    context.startService(playIntent)
                },
                onFailure = {
                    println("Error: $it")
                }
            )
            shouldLoad.value = false
        }
    }

    AlertDialog(
        onDismissRequest= onDismissRequest,
        title = {
            Text("Are you sure you want to remove this item?")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    deleteTheItem(selectedData.watchUrl)
                    onDismissRequest()
                },

                ) {
                Text("Remove")
            }
        },
         dismissButton = {
             TextButton(
            onClick = {
                shouldLoad.value = true
                onDismissRequest()
            },

        ) {
            Text("Play in background!")
        }

        }
    )
}

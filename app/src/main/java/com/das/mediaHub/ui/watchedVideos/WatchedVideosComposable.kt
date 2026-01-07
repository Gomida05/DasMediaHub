package com.das.mediaHub.ui.watchedVideos

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
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.das.mediaHub.MainActivity
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

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    IconButton(
                        onClick = {
                            backStack.add(Saved)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Saved videos",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        )
                    }
                },
                title = {
                    Text(
                        "Recently watched videos",
                        style = MaterialTheme.typography.headlineSmall
                            .copy(textAlign = TextAlign.Center),
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
                .fillMaxWidth()
        ) {
            if (isLoading) {
                item {
                    CircularProgressIndicator()
                }
            } else if (!isError.isNullOrEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxSize()
                    ) {
                        Text(
                            text = isError.toString(),
                            style = MaterialTheme.typography.bodyMedium
                                .copy(color = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                        )
                    }
                }
            } else {
                if (searchResults.isEmpty()) {

                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                            ) {
                                Text(
                                    text = "You haven't watched any video!.",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                )
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    "",

                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .size(60.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(searchResults, key = { it.watchUrl }) { searchItem ->
                        WatchedMedia(
                            backStack,
                            dbHelper = dbHelper,
                            searchItem,
                            viewModel
                        )

                    }
                }
            }
        }
    }
}

@Composable
private fun WatchedMedia(
    backStack: NavBackStack<NavKey>,
    dbHelper: WatchHistory,
    selectedItem: SavedVideosListData,
    viewModel: WatchedVideosViewModel
) {

    val showDialog = remember { mutableStateOf(false) }

    val showInfoDialog = remember { mutableStateOf(false) }

    val videoId = selectedItem.watchUrl
    val title = selectedItem.title
    val viewsNumber = selectedItem.viewer
    val dateOfVideo = selectedItem.dateTime
    val channelName = selectedItem.channelName
    val duration = selectedItem.duration
    val videoThumbnailURL = selectedItem.thumbnailUrl
    val channelThumbnails = selectedItem.channelThumbnail

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
                        dbHelper = dbHelper,
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
                        model = channelThumbnails,
                        error = painterResource(R.mipmap.ic_launcher_ofme),
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
            selectedItem,
            deleteTheItem = { selectedId->
                WatchHistory(context).deleteWatchUrl(selectedId)
                viewModel.removeSearchItem(selectedItem)
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
                    painter = painterResource(R.mipmap.ic_launcher_ofme),
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
    dbHelper: WatchHistory,
    selectedId: String,
    controller: NavBackStack<NavKey>
) {
    try {
        val title = dbHelper.getVideoTitle(selectedId)
        /*
        val viewNumber = dbHelper.getViewNumber(selectedId)
        val datVideo = dbHelper.getVideoDate(selectedId)
        val videoChannel = dbHelper.getVideoChannelName(selectedId)
        val ourDuration = dbHelper.getDuration(selectedId).toString()
        val channelThumbnail = dbHelper.getChannelNameThumbnail(selectedId)
        val bundle = Bundle().apply {
            putString("View_ID", selectedId)
            putString("View_URL", "https://www.youtube.com/watch?v=$selectedId")
            putString("View_Title", title)
            putString("View_Number", viewNumber)
            putString("dateOfVideo", datVideo)
            putString("channelName", videoChannel)
            putString("duration", ourDuration)
            putString("channel_Thumbnails", channelThumbnail)
        }*/
        controller.add(VideoViewer(
            Video(
                id =selectedId,
                title = title
            )
        ))

    } catch (e: Exception) {
        showNotificationDialog = TopPopUp(
            message = "Error: ${e.message}",
            icon = Icons.Default.VideoLibrary,
            loading = false
        )
    }
}

@Composable
private fun ShowAlertDialog(
    context: Context,
    selectedData: SavedVideosListData,
    deleteTheItem: (selectedId: String) -> Unit,
    onDismissRequest: () ->Unit
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

package com.das.forui.ui.settings.watch_later

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.data.databased.DatabaseFavorite
import com.das.forui.data.constants.Action.ACTION_START
import com.das.forui.data.constants.Intents.NEW_INTENT_FOR_VIEWER
import com.das.forui.data.model.SavedVideosListData
import com.das.forui.data.model.VideosListData
import com.das.forui.services.AudioServiceFromUrl
import com.das.forui.ui.viewer.GlobalVideoList.bundles



@Composable
fun WatchLaterComposable(navController: NavController) {


    val viewModel: WatchLaterViewModel = viewModel()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val searchResults by viewModel.searchResults
    val isLoading by viewModel.isLoading

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
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            } else {
                if (searchResults.isEmpty()) {

                    item {
                        Text(
                            text = "You don't have any saved videos int your collection!." +
                                    "\nSave some videos to add to your collection! ",
                            style = MaterialTheme.typography.headlineMedium
                                .copy(textAlign = TextAlign.Center),
                            fontSize = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "",
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                } else {
                    items(searchResults, key = { it.watchUrl }) { searchItem ->
                        CategoryItems(
                            navController,
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
private fun CategoryItems(
    navController: NavController,
    selectedItem: SavedVideosListData,
    viewModel: WatchLaterViewModel
) {

    var showDialog by remember { mutableStateOf(false) }

    var showInfoDialog by remember { mutableStateOf(false) }

    val videoId = selectedItem.watchUrl
    val title = selectedItem.title
    val viewsNumber = selectedItem.viewer
    val dateOfVideo = selectedItem.dateTime
    val channelName = selectedItem.channelName
    val duration = selectedItem.duration
    val videoThumbnailURL = selectedItem.thumbnailUrl
    val channelThumbnails = selectedItem.channelThumbnail

    val context = LocalContext.current


    val imageRequest = remember {
        ImageRequest.Builder(context)
            .data(videoThumbnailURL)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

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
                        navController
                    )
                },
                onLongClick = {
                    showDialog = true
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
                    model = imageRequest,
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
                        showInfoDialog = true
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
                        showDialog = true
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
    if (showDialog){
        ShowAlertDialog(
            context,
            selectedItem,
            viewModel,
            deleteTheItem = { selectedId->
                DatabaseFavorite(context).deleteWatchUrl(selectedId)
                viewModel.removeSearchItem(selectedItem)
            },
            onDismissRequest = {showDialog = false}
        )
    }
    if (showInfoDialog){
        InfoDialog{
            showInfoDialog = false
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
        controller: NavController
    ) {
    try {
        val dbHelper = DatabaseFavorite(context)
        val viewNumber = dbHelper.getViewNumber(selectedId)
        val datVideo = dbHelper.getVideoDate(selectedId)
        val videoChannel = dbHelper.getVideoChannelName(selectedId)
        val ourDuration = dbHelper.getDuration(selectedId).toString()
        val title = dbHelper.getVideoTitle(selectedId)
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
        }
        bundles.putBundle(NEW_INTENT_FOR_VIEWER, bundle)
        controller.navigate("video viewer")

    } catch (e: Exception) {
        MainActivity().alertUserError(context, e.message.toString())
    }
}

@Composable
private fun ShowAlertDialog(
    context: Context,
    selectedData: SavedVideosListData,
    viewModel: WatchLaterViewModel,
    deleteTheItem: (selectedId: String) -> Unit,
    onDismissRequest: () ->Unit
){

    var shouldLoad by remember { mutableStateOf(false) }

    if (shouldLoad) {
        LaunchedEffect(Unit) {
            viewModel.getListItemsStreamUrls(
                VideosListData(
                    selectedData.watchUrl, selectedData.title, selectedData.viewer,
                    selectedData.dateTime, selectedData.duration, selectedData.channelName, ""
                ),
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
            shouldLoad = false
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
                shouldLoad = true
                onDismissRequest()
            },

        ) {
            Text("Play in background!")
        }

        }
    )
}

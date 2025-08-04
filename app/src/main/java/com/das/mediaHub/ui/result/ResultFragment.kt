package com.das.mediaHub.ui.result

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.das.mediaHub.MainActivity
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.constants.Intents.NEW_INTENT_FOR_VIEWER
import com.das.mediaHub.data.model.SearchResultFromMain
import com.das.mediaHub.services.AudioServiceFromUrl
import com.das.mediaHub.data.model.VideosListData
import com.das.mediaHub.data.constants.GlobalVideoList.bundles
import com.das.mediaHub.NavScreens.VideoViewer
import com.das.mediaHub.data.YouTuber.loadStreamUrl
import com.das.mediaHub.ui.viewer.CustomMethods.SkeletonSuggestionLoadingLayout

@Composable
fun ResultViewerPage(navController: NavController, data: String) {

    val viewModel = viewModel<ResultViewModel>()
    val isLoading by rememberSaveable { viewModel.isLoading }
    val searchResults by rememberSaveable { viewModel.searchResults }
    val foundError by rememberSaveable { viewModel.error }

    val mContext = LocalContext.current


    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            viewModel.fetchSuggestions(data)
        }
    }


    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                },
                actions = {
                    Button(
                        onClick = {
                            navController.navigateUp()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 1.dp, top = 1.dp)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Outlined.Search),
                            contentDescription = "Back"
                        )
                        Text(data)
                    }

                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    )
    { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize(),
        ) {

            if (isLoading) {
                item {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        SkeletonSuggestionLoadingLayout(true)
                    }
                }
            } else {
                when {
                    searchResults.isEmpty() -> item {
                        Text(
                            text = "No results found for \n$data",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    !foundError.isNullOrEmpty() -> item {
                        Text(
                            text = "Something went wrong, please check your internet and try again!",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    else -> {
                        items(
                            searchResults, key = { it.videoId }
                        ) { searchItem ->

                            VideoItems(
                                mContext,
                                navController,
                                searchItem
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun VideoItems(
    context: Context,
    navController: NavController,
    searchItem: SearchResultFromMain
) {
    val videoId = searchItem.videoId
    val title = searchItem.title
    val viewsNumber = searchItem.views
    val dateOfVideo = searchItem.dateOfVideo
    val channelName = searchItem.channelName
    val duration = searchItem.duration
    val videoThumbnailURL = searchItem.videoId
    val channelThumbnails = searchItem.channelThumbnailsUrl

    var showDialog by remember { mutableStateOf(false) }

    val imageRequest = remember {
        ImageRequest.Builder(context)
            .data("https://img.youtube.com/vi/$videoThumbnailURL/0.jpg")
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(1))
            .fillMaxWidth()
            .padding(bottom = 3.dp, top = 3.dp)
            .combinedClickable(
                onClick = {
                    val bundle = Bundle().apply {
                        putString("View_ID", videoId)
                        putString("View_URL", "https://www.youtube.com/watch?v=$videoId")
                        putString("View_Title", title)
                        putString("View_Number", viewsNumber)
                        putString("dateOfVideo", dateOfVideo)
                        putString("channelName", channelName)
                        putString("duration", duration)
                        putString("channel_Thumbnails", channelThumbnails)
                    }
                    bundles.putBundle(NEW_INTENT_FOR_VIEWER, bundle)
                    navController.navigate(VideoViewer.route)

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
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(end = 3.dp, bottom = 3.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xCC2C2B2B), RoundedCornerShape(5.dp))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {


                IconButton(
                    onClick = {
                        Toast.makeText(context, channelName, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(channelThumbnails)
                            .crossfade(true)
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
                        contentDescription = "More option"
                    )
                }
            }
        }
    }

    if (showDialog) {
        ShowAlertDialog(
            mContext = context,
            selectedItem = VideosListData(
                videoId, title, viewsNumber, dateOfVideo,
                duration, channelName, channelThumbnails
            ),
            onDismissRequest = { showDialog = false }
        )
    }
}

@Composable
private fun ShowAlertDialog(
    mContext: Context,
    selectedItem: VideosListData,
    onDismissRequest: () ->Unit
){
    val thumbnailUrl = "https://img.youtube.com/vi/${selectedItem.videoId}/0.jpg"

    var shouldLoad by remember { mutableStateOf(false) }

    LaunchedEffect(shouldLoad) {
        if (shouldLoad) {
            loadStreamUrl(
                selectedItem,
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
                    mContext.startService(playIntent)
                },
                onFailure = {
                    println("Error: $it")
                }
            )
            shouldLoad = false
        }
    }


    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(13.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Do you want to download it as video or audio?",
                    modifier = Modifier.padding(8.dp),
                )
                AsyncImage(
                    model = ImageRequest.Builder(mContext)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Category Image",
                    modifier = Modifier
                        .height(190.dp)
                        .clip(RoundedCornerShape(4)),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = {
                            shouldLoad = true
                        },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Text("Background")
                    }
                    TextButton(
                        onClick = {
                            MainActivity().startDownloadingAudio(
                                mContext,
                                selectedItem.videoId,
                                selectedItem.title
                            )
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Text("Music")
                    }
                    TextButton(
                        onClick = {
                            MainActivity().startDownloadingVideo(
                                mContext,
                                selectedItem.videoId,
                                selectedItem.title
                            )
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Text("Video")
                    }
                }
            }
        }
    }

}
package com.das.forui.ui.downloads

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.das.forui.R
import com.das.forui.databased.PathSaver.getVideosDownloadPath
import com.das.forui.databased.PathSaver.getAudioDownloadPath
import com.das.forui.objectsAndData.ForUIDataClass.DownloadedListData
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_START
import com.das.forui.objectsAndData.ForUIKeyWords.PLAY_HERE_VIDEO
import com.das.forui.services.BackGroundPlayer
import com.das.forui.ui.viewer.GlobalVideoList.bundles
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsPageComposable(navController: NavController) {

    val viewModel: DownloadsPageViewModel = viewModel()

    val mContext = LocalContext.current

    var isVideo by remember { mutableStateOf(true) }

    val downloadedListData by viewModel.downloadedListData.collectAsState()

    val videoPath = getVideosDownloadPath(mContext)
    val audioPath = getAudioDownloadPath(mContext)




    LaunchedEffect(isVideo) {
        // Fetch data based on the file type
        if (isVideo) {
            viewModel.fetchDataFromDatabase(videoPath, 1)
        } else {
            viewModel.fetchDataFromDatabase(audioPath, 0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Downloads",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                },
                navigationIcon = {
                    ElevatedButton(
                        onClick = {
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.AutoMirrored.Default.ArrowBack),
                            ""
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Settings),
                            ""
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .wrapContentSize(Alignment.Center)
                .fillMaxSize()
        ) {

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                ElevatedButton(
                    onClick = {
                        isVideo = true
                        viewModel.fetchDataFromDatabase(videoPath, 1)
                    },
                    enabled = !isVideo
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.VideoLibrary),
                        ""
                    )
                    Text(
                        text = "Videos"
                    )
                }
                ElevatedButton(
                    onClick = {
                        isVideo = false
                        viewModel.fetchDataFromDatabase(videoPath, 1)
                    },
                    enabled = isVideo
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.LibraryMusic),
                        ""
                    )
                    Text(
                        text = "Musics"
                    )
                }
            }

            if (downloadedListData.isEmpty()) {

                Text(
                    text = "You haven't saved any ${if (isVideo) "videos" else "music"} yet. Save some to create your collection!",
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn {
                    items(downloadedListData, key = { it.title }) { searchItem ->
                        ListItems(searchItem, isVideo, mContext, navController)

                    }
                }
            }

        }
    }
}


@Composable
fun ListItems(
    itemDetails: DownloadedListData,
    isVideo: Boolean,
    mContext: Context,
    navController: NavController
) {


    var showAlertDialog by remember { mutableStateOf(false) }

    Card(
        onClick = {
            itemClicked(
                itemDetails.pathOfVideo.toString(),
                itemDetails.title,
                isVideo,
                mContext,
                navController
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp)
            .clip(RoundedCornerShape(30))

    ) {


        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .height(65.dp)
                .padding(5.dp)
        ) {
            if (isVideo) {
                val model = ImageRequest.Builder(mContext)
                    .data(itemDetails.thumbnailUri)
                    .videoFrameMillis(10000)
                    .decoderFactory { result, options, _ ->
                        VideoFrameDecoder(
                            result.source,
                            options
                        )
                    }
                    .error(R.drawable.smart_display_24dp)
                    .build()
                AsyncImage(
                    model = model,
                    "loaded thumbnail ${itemDetails.fileSize}",
                    modifier = Modifier
                        .size(65.dp, 65.dp)
                        .align(Alignment.CenterVertically)
                        .clip(RoundedCornerShape(15)),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    imageVector = Icons.Default.MusicNote,
                    "",
                    modifier = Modifier
                        .size(65.dp, 65.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            Column(
                modifier = Modifier
                    .width(250.dp)
            ) {
                Text(
                    text = itemDetails.title,
                    fontSize = 16.sp,
                    maxLines = 2,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()

                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = itemDetails.dateTime,
                        fontSize = 13.sp,
                        maxLines = 1,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .width(95.dp)
                    )
                    Text(
                        text = itemDetails.fileSize,
                        fontSize = 13.sp,
                        maxLines = 1,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .width(95.dp)
                    )
                }
            }

            IconButton(
                onClick = {
                    showAlertDialog = true
                },
                modifier = Modifier
                    .size(45.dp, 45.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Default.MoreVert),
                    ""
                )
            }
        }

    }

    if (showAlertDialog){
        DeleteItem(
            onDismissRequest = {showAlertDialog = false},
            onDelete = {
                File(itemDetails.pathOfVideo.path!!).delete()
            }
        )
    }

}

private fun itemClicked(
    selectedFilePath: String,
    selectedTitle: String,
    isVideo: Boolean,
    context: Context,
    navController: NavController
) {


    if (isVideo) {
        bundles.putString(PLAY_HERE_VIDEO, selectedFilePath)
        navController.navigate("ExoPlayerUI")
    } else {

        val playIntent = Intent(context, BackGroundPlayer::class.java).apply {
            action = ACTION_START
            putExtra("media_id", selectedFilePath)
            putExtra("media_url", selectedFilePath)
            putExtra("title", selectedTitle)
        }
        context.startService(playIntent)

    }
}

@Composable
private fun DeleteItem(
    onDismissRequest: ()-> Unit,
    onDelete: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Are you sure you want to delete this file?")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDelete()
                    onDismissRequest()
                }
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("No")
            }
        }
    )
}


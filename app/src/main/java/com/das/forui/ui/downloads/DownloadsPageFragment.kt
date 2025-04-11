package com.das.forui.ui.downloads

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.Image
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.das.forui.R
import com.das.forui.databased.PathSaver
import com.das.forui.objectsAndData.DownloadedListData
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_START
import com.das.forui.objectsAndData.ForUIKeyWords.MEDIA_TITLE
import com.das.forui.objectsAndData.ForUIKeyWords.PLAY_HERE_VIDEO
import com.das.forui.services.BackGroundPlayer
import com.das.forui.ui.viewer.GlobalVideoList.bundles
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsPageComposable(navController: NavController) {


        val mContext = LocalContext.current
        val applicationContext = LocalContext.current.applicationContext as Application

        val viewModel = DownloadsPageViewModel(applicationContext)

        var isVideo by remember { mutableStateOf(true) }

        val downloadedListData by viewModel.downloadedListData.collectAsState()

        val videoPath = PathSaver().getVideosDownloadPath(mContext)
        val audioPath = PathSaver().getAudioDownloadPath(mContext)




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
        var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

        LaunchedEffect(itemDetails.thumbnailUri) {
            val glideBitmap = Glide.with(mContext)
                .asBitmap()
                .load(itemDetails.thumbnailUri)
                .submit()
                .get()
                .asImageBitmap()
            bitmap = glideBitmap
        }

        Card(
            onClick = {
                itemClicked(
                    itemDetails,
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
                    Image(
                        bitmap = bitmap ?: ImageBitmap.imageResource(R.drawable.smart_display_24dp),
                        "loaded thumbnail ${itemDetails.fileSize}",
                        modifier = Modifier
                            .size(65.dp, 65.dp)
                            .align(Alignment.CenterVertically)
                            .clip(RoundedCornerShape(4)),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.MusicNote),
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
                        deleteItem(
                            mContext,
                            onDelete = {
                            File(itemDetails.pathOfVideo.path!!).delete()
                            }
                        )
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

    }

    private fun itemClicked(
        selectedItem: DownloadedListData,
        isVideo: Boolean,
        context: Context,
        navController: NavController
    ) {
        val filePath = selectedItem.pathOfVideo.toString()
        val title = selectedItem.title

        println("here is the file name: $filePath")
        if (isVideo) {
            val bundle = Bundle().apply {
                putString(PLAY_HERE_VIDEO, filePath)
                putString(MEDIA_TITLE, title)
            }
            bundles.putString(PLAY_HERE_VIDEO, filePath)
            navController.navigate("ExoPlayerUI")
        } else {

            val playIntent = Intent(context, BackGroundPlayer::class.java).apply {
                action = ACTION_START
                putExtra("media_id", filePath)
                putExtra("media_url", filePath)
                putExtra("title", title)
            }
            context.startService(playIntent)

        }
    }

    private fun deleteItem(
        context: Context,
        onDelete: () -> Unit,
    ) {

        AlertDialog.Builder(context)
            .setTitle("Are you sure you want to delete this file?")
            .setPositiveButton("Yes") { _, _ ->
                onDelete()
            }
            .setNegativeButton("No") { _, _ -> }
            .show()

    }


    @Composable
    fun getVideoThumbnail(
        mContext: Context,
        fileUri: Uri
    ): ImageBitmap {

        var videoThumbnail by remember { mutableStateOf(ImageBitmap.imageResource(mContext.resources,R.drawable.smart_display_24dp)) }
        Glide.with(LocalContext.current)
            .asBitmap()
            .load(fileUri)
            .error(
                R.drawable.smart_display_24dp
            )
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Pass the loaded bitmap back via the callback
                    videoThumbnail = resource.asImageBitmap()
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })
        return videoThumbnail

    }


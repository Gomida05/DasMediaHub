package com.das.mediaHub.ui.downloads

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.das.mediaHub.R
import com.das.mediaHub.NavScreens
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.constants.Playback.PLAY_HERE_VIDEO
import com.das.mediaHub.services.BackGroundPlayer
import com.das.mediaHub.python.YouTuber.mediaItems
import com.das.mediaHub.data.constants.GlobalVideoList.bundles
import com.das.mediaHub.data.local.PathSaver
import java.io.File
import kotlin.collections.set

@Composable
fun DownloadsComposable(navController: NavController, tabIndex: Int = 0) {

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(tabIndex) }

    val viewModel = viewModel<DownloadsPageViewModel>()

    val videosListData by viewModel.videosListData

    val musicsListData by viewModel.listMusic

    val isLoading by viewModel.isLoading
    val errorFound by viewModel.errorFound

    val mContext = LocalContext.current

    val pathSaver = remember {
        PathSaver(mContext)
    }




    val videoPath = remember {
        pathSaver.getVideosDownloadPath()
    }
    val audioPath = remember {
        pathSaver.getAudioDownloadPath()
    }
    val tabs = PageEnum.entries


    val topAppBarState = TopAppBarDefaults.enterAlwaysScrollBehavior()


    LaunchedEffect(Unit) {
        viewModel.fetchVideoFiles(videoPath)
        viewModel.fetchMusicFiles(audioPath)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = topAppBarState,
                title = {
                    CustomTabRow(
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it },
                        tabs = tabs
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .nestedScroll(topAppBarState.nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .wrapContentSize(Alignment.Center)
                .fillMaxSize()
        ) {

            if (selectedTabIndex == 0 && videosListData.isEmpty() || selectedTabIndex != 0 && musicsListData.isEmpty()) {

                item {
                    Text(
                        text = "You haven't saved any ${if (selectedTabIndex == 0) "videos" else "music"} yet. Save some to create your collection!",
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (isLoading){
                item {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            } else if (!errorFound.isNullOrEmpty()){
                item {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = errorFound.toString(),
                            style = MaterialTheme.typography.bodyMedium
                                .copy(color = MaterialTheme.colorScheme.error),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            } else {
                if (selectedTabIndex == 0) {
                    itemsIndexed(videosListData) { index, searchItem ->
                        ListItems(
                            itemDetails = searchItem,
                            isVideo = true,
                            mContext
                        ) {
                            itemClicked(
                                index,
                                searchItem.mediaId,
                                true,
                                mContext,
                                navController
                            )
                        }
                    }
                } else {
                    mediaItems = musicsListData.toMutableList()
                    itemsIndexed(musicsListData) { index, searchItem ->

                        ListItems(
                            searchItem,
                            false,
                            mContext
                        ) {
                            itemClicked(
                                index,
                                searchItem.mediaId,
                                false,
                                mContext,
                                navController
                            )
                        }

                    }
                }
            }

        }
    }
}

@Composable
fun ListItems(
    itemDetails: MediaItem,
    isVideo: Boolean,
    mContext: Context,
    onClick: () -> Unit
) {


    var showAlertDialog by remember { mutableStateOf(false) }



    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(15),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.2f
            )
        ),
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth()

    ) {


        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .height(65.dp)
                .align(Alignment.CenterHorizontally)
        ) {

            if (isVideo) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(mContext)
                            .data(itemDetails.mediaId.toUri())
                            .videoFrameMillis(10000)
                            .decoderFactory { result, options, _ ->
                                VideoFrameDecoder(
                                    result.source,
                                    options
                                )
                            }
                            .error(R.mipmap.under_development)
                            .build(),
                    contentDescription = "loaded thumbnail ${itemDetails.mediaMetadata.artist}",
                    modifier = Modifier
                        .size(65.dp, 65.dp)
                        .align(Alignment.CenterVertically)
                        .clip(RoundedCornerShape(10)),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "loaded thumbnail ${itemDetails.mediaMetadata.artist}",
                    modifier = Modifier
                        .size(65.dp, 65.dp)
                        .align(Alignment.CenterVertically)
                        .clip(RoundedCornerShape(10)),
                )
            }

            Column(
                modifier = Modifier
//                    .width(250.dp)
                    .fillMaxWidth(0.86f)
            ) {
                Text(
                    text = itemDetails.mediaMetadata.title.toString(),
                    fontSize = 14.sp,
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
                        text = itemDetails.mediaMetadata.description.toString(),
                        fontSize = 13.sp,
                        maxLines = 1,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .width(95.dp)
                    )
                    Text(
                        text = itemDetails.mediaMetadata.artist.toString(),
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

    if (showAlertDialog) {
        DeleteItem(
            onDismissRequest = {
                showAlertDialog = false
            },
            onDelete = {
                itemDetails.mediaId.toUri().path?.let { File(it).delete() }
            }
        )
    }

}

private fun itemClicked(
    index: Int,
    selectedFilePath: String,
    isVideo: Boolean,
    context: Context,
    navController: NavController
) {


    if (isVideo) {
        bundles.putString(PLAY_HERE_VIDEO, selectedFilePath)
        navController.navigate(NavScreens.ExoPlayerUI.route)
    } else {

        val playIntent = Intent(context, BackGroundPlayer::class.java).apply {
            action = ACTION_START
            putExtra("media_id", index)
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


@Composable
private fun CustomTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<PageEnum>
) {
    val haptic = LocalHapticFeedback.current
    val tabWidths = remember { mutableStateMapOf<Int, Float>() }
    val density = LocalDensity.current
    val indicatorOffset by animateDpAsState(
        targetValue = tabWidths.entries.take(selectedTabIndex).sumOf { it.value.toDouble() }.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "indicatorOffset"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = tabWidths[selectedTabIndex]?.dp ?: 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "indicatorWidth"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        // Animated underline indicator
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(indicatorWidth)
                .height(3.dp)
                .align(Alignment.BottomStart)
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .zIndex(1f)
        )

        // Tab items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, mode ->
                val isSelected = selectedTabIndex == index
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "textColor"
                )
                val iconScale by animateDpAsState(
                    targetValue = if (isSelected) 1.2.dp else 1.dp,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                    label = "iconScale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(index)
                        }
                        .onGloballyPositioned {
                            tabWidths[index] = it.size.width.toFloat() / density.density
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                PageEnum.VIDEOS -> Icons.Filled.VideoLibrary
                                PageEnum.AUDIOS -> Icons.Filled.LibraryMusic
                            },
                            contentDescription = mode.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            tint = textColor,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    scaleX = iconScale.value
                                    scaleY = iconScale.value
                                }
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = mode.name
                                .lowercase()
                                .replace('_', ' ')
                                .replaceFirstChar { it.uppercase() },
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
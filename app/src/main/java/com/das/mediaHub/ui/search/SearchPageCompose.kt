package com.das.mediaHub.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.das.mediaHub.MainActivity
import com.das.mediaHub.NavScreens
import com.das.mediaHub.python.YouTuber.youtubeExtractor
import com.das.mediaHub.python.YouTuber.isValidYoutubeURL
import com.das.mediaHub.python.YouTuber.extractPlaylistId
import com.das.mediaHub.python.YouTuber.isValidYouTubePlaylistUrl
import com.das.mediaHub.NavScreens.ResultViewerPage
import com.das.mediaHub.data.model.SearchData
import com.das.mediaHub.data.model.searcher.Video
import kotlinx.coroutines.launch

@Composable
fun SearchPageCompose(
    backStack: NavBackStack<NavKey>,
    newText: String
) {
    val viewMode = viewModel(modelClass = SearchPageViewMode::class.java, key = "SearchPageViewMode_$newText")

    val searchHistory by viewMode.searchHistory
    val isThereError by viewMode.error
    val isLoading by viewMode.isLoading

    val topAppBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val scope = rememberCoroutineScope()
    val snackBar = remember { SnackbarHostState() }

    val playListUrl = retain { mutableStateOf("") }
    val askToDownloadPlayList = retain { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewMode.fetchDatabase()
    }


    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBar)
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .nestedScroll(topAppBarScroll.nestedScrollConnection)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(17.dp))
            OutlinedTextField(
                value = viewMode.query.value,

                onValueChange = {
                    viewMode.setQuery(it)
                },
                placeholder = {
                    Text(
                        text = "Enter key words or Insert URL"
                    )
                },
                shape = RoundedCornerShape(28),
                singleLine = true,
                modifier = Modifier
                    .padding(6.dp)
                    .fillMaxWidth()
                    .height(55.dp)
                    .align(Alignment.CenterHorizontally),

                textStyle = MaterialTheme.typography.bodyMedium,
                leadingIcon = {
                    IconButton(
                        onClick = {
                            backStack.removeLastOrNull()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            "navigateUpButton"
                        )
                    }
                },
                trailingIcon = {
                    if (viewMode.query.value.isNotEmpty()) {
                        IconButton(onClick = { viewMode.query.value = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text,
                    showKeyboardOnFocus = true,
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (viewMode.query.value.isNotBlank()) {
                            keyEvent(
                                backStack = backStack,
                                editTextText = viewMode.query.value,
                                addIt = {
                                    viewMode.addNew(
                                        it
                                    )
                                },
                                isPlayList = { url ->
                                    askToDownloadPlayList.value = true
                                    playListUrl.value = url
                                }
                            ) {
                                scope.launch {
                                    snackBar.showSnackbar(it)
                                }
                            }
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            if (!isThereError.isNullOrEmpty()) {
                Text(
                    text = isThereError ?: "Unknown error",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            else if (searchHistory.isNotEmpty()){
                LazyVerticalGrid (
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(5.dp)
                ) {
                    items(
                        items = searchHistory,
                        key = { it.id }
                    ) { settingsItem ->

                        RecentlySearchList(
                            title = settingsItem.value,
                            settingsResults = settingsItem,
                            deleteThis = {
                                viewMode.deleById(it)
                            },
                            onButtonClicked = { text ->
                                viewMode.query.value = text
                                backStack.add(ResultViewerPage(text))
                            }
                        )
                    }
                }
            }


        }

    }
    if (askToDownloadPlayList.value && playListUrl.value.isNotEmpty()){

        PlayListDownloadRequest(
            onDismissRequest = {
                askToDownloadPlayList.value = false
            },
            playListUrl.value
        )

    }

}




@Composable
private fun RecentlySearchList(
    title: String,
    settingsResults: SearchData,
    deleteThis: (String) -> Unit,
    onButtonClicked: (text: String) -> Unit
) {
    val showDeleteConfirm = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = {
                onButtonClicked(title)
            },
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }


        IconButton(
            onClick = { showDeleteConfirm.value = true }
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Search",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    if (showDeleteConfirm.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm.value = false },
            title = { Text("Remove Search") },
            text = { Text("Are you sure you want to remove \"$title\" from your search history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteThis(settingsResults.id)
                        showDeleteConfirm.value = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun PlayListDownloadRequest(onDismissRequest: ()->Unit, url: String){

    val mContext = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismissRequest,

        title = {
            Text(
                "Do you want to download the playlist",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            AsyncImage(
                model = ImageRequest.Builder(mContext)
                    .data("https://img.youtube.com/vi/${extractPlaylistId(url)}/0.jpg")
                    .crossfade(true)
                    .build(),
                contentDescription = "Category Image",
                modifier = Modifier
                    .height(190.dp)
                    .clip(RoundedCornerShape(4)),
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit
            )

        },
        confirmButton = {
            ElevatedButton (
                onClick = {
                    onDismissRequest()
                    MainActivity().startPlayListDownload(
                        playListUrl = url
                    )

                }
            ) {

                Text(
                    "Download"
                )

                Icon(
                    imageVector = Icons.Default.Download,
                    ""
                )

            }
        },
        dismissButton = {
            ElevatedButton(
                onClick = onDismissRequest
            ) {

                Text(
                    "No"
                )
                Spacer(
                    modifier = Modifier.width(5.dp)
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    ""
                )
            }

        }
    )
}


private fun keyEvent(
    backStack: NavBackStack<NavKey>,
    editTextText: String,
    addIt: (String) -> Unit,
    isPlayList: (url: String) -> Unit,
    error: (String) -> Unit
) {
    try {
        when {
            editTextText.isValidYoutubeURL() -> {
                val videoId = editTextText.youtubeExtractor()
                backStack.add(NavScreens.VideoViewer(
                    Video(
                        id = videoId.toString()
                    ))
                )

            }
            editTextText.isValidYouTubePlaylistUrl() -> {
                isPlayList(editTextText)
            }
            else -> {
                addIt(editTextText)
                backStack.add(ResultViewerPage(editTextText))

            }
        }
    } catch (e: Exception) {
        error(e.message ?: "Unknown error")
    }
}








package com.das.forui.ui.home.searcher

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.das.forui.MainActivity
import com.das.forui.data.Youtuber.youtubeExtractor
import com.das.forui.data.Youtuber.isValidYoutubeURL
import com.das.forui.data.constants.Intents.NEW_INTENT_FOR_VIEWER
import com.das.forui.data.constants.Intents.NEW_TEXT_FOR_RESULT
import com.das.forui.data.Youtuber.extractPlaylistId
import com.das.forui.data.Youtuber.isValidYouTubePlaylistUrl
import com.das.forui.ui.viewer.GlobalVideoList.bundles
import com.das.forui.Screen.ResultViewerPage
import com.das.forui.data.databased.room.dataclass.SearchData

@Composable
fun SearchPageCompose(
    navController: NavController,
    newText: String
) {
    val context = LocalContext.current
    val viewMode: SearchPageViewMode = viewModel()

    val searchHistory by viewMode.searchHistory
    val isThereError by viewMode.error
    val isLoading by viewMode.isLoading

    val topAppBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val textState = remember { mutableStateOf(newText) }

    var playListUrl by remember { mutableStateOf("") }
    var askToDownloadPlayList by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewMode.fetchDatabase()
    }


    Scaffold(
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
                value = textState.value,
                onValueChange = { newText ->
                    textState.value = newText
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
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            "navigateUpButton"
                        )
                    }
                },
                trailingIcon = {
                    if (textState.value.isNotEmpty()) {
                        IconButton(onClick = { textState.value = "" }) {
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
                        if (textState.value.isNotBlank()) {
                            keyEvent(
                                context = context,
                                navController = navController,
                                editTextText = textState.value,
                                addIt = {
                                    viewMode.addNew(
                                        it
                                    )
                                }
                            ) { url ->
                                askToDownloadPlayList = true
                                playListUrl = url
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
                LazyColumn(
                    modifier = Modifier
                ) {
                    items(
                        items = searchHistory,
                        key = { it.id }
                    ) { settingsItem ->
                        RecentlySearchList(
                            context,
                            title = settingsItem.value,
                            settingsResults = settingsItem,
                            deleteThis = {
                                viewMode.deleById(it)
                            },
                            onButtonClicked = { text ->
                                textState.value = text
                                goSearch(context, navController, text)
                            }
                        )
                    }
                }
            }


        }

    }
    if (askToDownloadPlayList && playListUrl.isNotEmpty()){

        PlayListDownloadRequest(
            onDismissRequest = {
                askToDownloadPlayList = false
            },
            context,
            playListUrl
        )

    }

}




@Composable
private fun RecentlySearchList(
    context: Context,
    title: String,
    settingsResults: SearchData,
    deleteThis: (String) -> Unit,
    onButtonClicked: (text: String)-> Unit
) {

    Button(
        onClick = {
            onButtonClicked(title)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(top = 2.dp, bottom = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            IconButton(
                onClick = {

                    AlertDialog.Builder(context)
                        .setTitle("Are you sure you want to remove it from the list?")
                        .setPositiveButton("Yes") { _, _ ->
                            deleteThis(settingsResults.id)
                        }
                        .setNegativeButton("No") { _, _ ->
                        }
                        .show()


                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Default.Delete),
                    ""
                )
            }
        }


    }
}


@Composable
fun PlayListDownloadRequest(onDismissRequest: ()->Unit, mContext: Context, url: String){
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
                        mContext,
                        url
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
    context: Context,
    navController: NavController,
    editTextText: String,
    addIt: (String) -> Unit,
    isPlayList: (url: String) -> Unit
) {
    try {
        when {
            isValidYoutubeURL(editTextText) -> {
                val videoId = youtubeExtractor(editTextText)
                val bundled = Bundle().apply {
                    putString("View_ID", videoId)
                    putString("View_URL", "https://www.youtube.com/watch?v=$videoId")
                }

                // Safely use a globally declared Bundle
                bundles.putBundle(NEW_INTENT_FOR_VIEWER, bundled)

            }
            isValidYouTubePlaylistUrl(editTextText) -> {
                isPlayList(editTextText)
            }
            else -> {
                addIt(editTextText)
                goSearch(
                    context,
                    navController,
                    editTextText
                )
            }
        }
    } catch (e: Exception) {
        showDialogs(context, e.message ?: "Unknown error")
    }
}

private fun goSearch(
    context: Context,
    navController: NavController,
    text: String
) {
    try {
        bundles.putString(NEW_TEXT_FOR_RESULT, text)

        navController.navigate(ResultViewerPage.route)

    } catch (e: Exception) {
        showDialogs(context, e.message.toString())
    }
}


private fun showDialogs(context: Context, message: String){
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}







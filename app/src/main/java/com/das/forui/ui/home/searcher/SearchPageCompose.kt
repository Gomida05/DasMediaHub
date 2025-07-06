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
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.das.forui.objectsAndData.Youtuber.youtubeExtractor
import com.das.forui.objectsAndData.Youtuber.isValidYoutubeURL
import com.das.forui.databased.SearchHistoryDB
import com.das.forui.objectsAndData.ForUIKeyWords.NEW_INTENT_FOR_VIEWER
import com.das.forui.objectsAndData.ForUIKeyWords.NEW_TEXT_FOR_RESULT
import com.das.forui.objectsAndData.Youtuber.extractPlaylistId
import com.das.forui.objectsAndData.Youtuber.isValidYouTubePlaylistUrl
import com.das.forui.ui.viewer.GlobalVideoList.bundles
import com.das.forui.Screen.ResultViewerPage

@Composable
fun SearchPageCompose(
    navController: NavController,
    newText: String
) {

    val topAppBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val textState = remember { mutableStateOf(newText) }
    val context = LocalContext.current
    val androidViewMode: SearchPageViewMode = viewModel()

    var playListUrl by remember { mutableStateOf("") }
    var askToDownloadPlayList by remember { mutableStateOf(false) }


    val settingsResults = remember { androidViewMode.downloadedListData}

    val isThereError by androidViewMode.error


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
                                navController,
                                textState.value,
                                context
                            ) { url ->
                                askToDownloadPlayList = true
                                playListUrl = url
                            }
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            if (isThereError.isNotEmpty()){

            }
            else if (settingsResults.value.isNotEmpty()){
                LazyColumn(
                    modifier = Modifier
                ) {
                    items(settingsResults.value) { settingsItem ->
                        RecentlySearchList(
                            context,
                            title = settingsItem,
                            settingsResults,
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
    settingsResults: MutableState<List<String>>,
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
                            SearchHistoryDB(context).deleteSearchList(title)
                            settingsResults.value = settingsResults.value.filter { it != title }
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
    navController: NavController,
    editTextText: String,
    context: Context,
    isPlayList: (url: String)-> Unit
) {

        try {
            if (isValidYoutubeURL(editTextText)) {
                val videoId = youtubeExtractor(editTextText)
                val bundled = Bundle().apply {
                    putString("View_ID", videoId)
                    putString("View_URL", "https://www.youtube.com/watch?v=$videoId")
                }
                bundles.putBundle(NEW_INTENT_FOR_VIEWER, bundled)
            } else if (isValidYouTubePlaylistUrl(editTextText)){

                isPlayList(editTextText)
//                MainActivity().startPlayListDownload(
//                    context,
//                    editTextText
//                )
//                showDialogs(context, "PlayList Starting to download!")
            }
            else {
                SearchHistoryDB(context).insertData(editTextText)
                goSearch(
                    context,
                    navController,
                    editTextText
                )
            }
        } catch (e: Exception) {
            showDialogs(context,e.message.toString())
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







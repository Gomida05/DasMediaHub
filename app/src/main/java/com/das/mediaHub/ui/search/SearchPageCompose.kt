package com.das.mediaHub.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import com.das.mediaHub.data.local.SearchHistoryDB
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.navigation.NavScreens
import com.das.mediaHub.navigation.NavScreens.ResultViewerPage
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import com.das.python.YouTuber.extractPlaylistId
import com.das.python.YouTuber.isValidYouTubePlaylistUrl
import com.das.python.YouTuber.isValidYoutubeURL
import com.das.python.YouTuber.youtubeExtractor
import kotlinx.coroutines.launch

@Composable
fun SearchPageCompose(
    backStack: NavBackStack<NavKey>,
    newText: String
) {
    val context = LocalContext.current
    val searchDB = remember {
        SearchHistoryDB(context)
    }
    val viewMode = viewModel(
        modelClass = SearchPageViewMode::class.java.kotlin,
        factory = viewModelFactory {
            initializer {
                SearchPageViewMode(searchDB)
            }
        }
    )
    val focusRequester = retain { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val uiState by viewMode.searchHistory.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackBar = remember { SnackbarHostState() }

    val playListUrl = retain { mutableStateOf("") }
    val askToDownloadPlayList = retain { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewMode.fetchDatabase()
        focusRequester.requestFocus()
        viewMode.setQuery(TextFieldValue(newText, TextRange(viewMode.query.value.text.length)))
        keyboardController?.show()
    }

    RetainedEffect (Unit) {
        onRetire {
            keyboardController?.hide()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackBar) },
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Search",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Search Field Container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    tonalElevation = 2.dp
                ) {
                    OutlinedTextField(
                        value = viewMode.query.value,
                        onValueChange = { viewMode.setQuery(it) },
                        placeholder = {
                            Text(
                                "Keywords or YouTube URL",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (viewMode.query.value.text.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        viewMode.setQuery(TextFieldValue(""))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search,
                            capitalization = KeyboardCapitalization.None
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (viewMode.query.value.text.isNotBlank()) {
                                    keyEvent(
                                        backStack = backStack,
                                        editTextText = viewMode.query.value.text,
                                        addIt = { viewMode.addNew(it) },
                                        isPlayList = { url ->
                                            askToDownloadPlayList.value = true
                                            playListUrl.value = url
                                        }
                                    ) {
                                        scope.launch { snackBar.showSnackbar(it) }
                                    }
                                }
                            }
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (val newState = uiState) {
                    is UiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = newState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                                    .copy(textAlign = TextAlign.Center)
                            )
                        }
                    }

                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )

                        }
                    }

                    is UiState.Success -> {
                        Text(
                            text = "RECENT SEARCHES",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp, top = 8.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(items = newState.data, key = { it.id }) { item ->
                                HistoryItem(
                                    title = item.value,
                                    onDelete = { viewMode.deleById(item.id) },
                                    onClick = { text ->
                                        viewMode.setQuery(
                                            TextFieldValue(
                                                text,
                                                TextRange(text.length)
                                            )
                                        )
                                        backStack.add(ResultViewerPage(text))
                                    }
                                )
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    if (askToDownloadPlayList.value && playListUrl.value.isNotEmpty()) {
        PlayListDownloadDialog(
            url = playListUrl.value,
            onDismiss = { askToDownloadPlayList.value = false }
        )
    }
}

@Composable
private fun HistoryItem(
    title: String,
    onDelete: () -> Unit,
    onClick: (String) -> Unit
) {
    val showDeleteConfirm = retain { mutableStateOf(false) }

    Surface(
        onClick = { onClick(title) },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showDeleteConfirm.value = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showDeleteConfirm.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm.value = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Remove from history?") },
            text = { Text("Do you want to delete \"$title\" from your recent searches?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm.value = false
                }) {
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
fun PlayListDownloadDialog(url: String, onDismiss: () -> Unit) {

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                "Download Playlist?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = "https://img.youtube.com/vi/${extractPlaylistId(url)}/0.jpg",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Would you like to start downloading all videos in this playlist?")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showNotificationDialog = TopPopUp(
                        message = "Sorry this feature is currently underdevelopment",
                        icon = Icons.Default.Error
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
                if (videoId.isNullOrEmpty()) return
                backStack.add(NavScreens.OnlineVideoPlayer(videoId = videoId))
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

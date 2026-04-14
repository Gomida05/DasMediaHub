package com.das.mediaHub.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.das.mediaHub.MainApplication
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.data.repository.SearchRepository
import com.das.mediaHub.navigation.NavScreens
import com.das.mediaHub.navigation.NavScreens.ResultViewerPage
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
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
    val app = context.applicationContext as MainApplication

    val repository = retain {
        SearchRepository(app.appDatabase.searchDatabase.searchHistoryDao())
    }

    val viewMode = viewModel(
        modelClass = SearchPageViewModel::class.java.kotlin,
        factory = viewModelFactory {
            initializer {
                SearchPageViewModel(repository)
            }
        }
    )

    val focusRequester = retain { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewMode.searchHistory.collectAsStateWithLifecycle()
    val query by viewMode.query.collectAsStateWithLifecycle()


    val scope = rememberCoroutineScope()
    val snackBar = remember { SnackbarHostState() }

    val playListUrl = retain { mutableStateOf("") }
    val askToDownloadPlayList = retain { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        viewMode.seedQueryIfEmpty(newText)
        keyboardController?.show()
    }

    DisposableEffect(Unit) {

        onDispose {
            keyboardController?.hide()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBar) },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
                        )
                    )
                )
                .padding(padding)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val compact = maxWidth < 420.dp
                val gridMinSize = when {
                    maxWidth < 420.dp -> 280.dp
                    maxWidth < 700.dp -> 180.dp
                    else -> 220.dp
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (compact) 16.dp else 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    SearchHero(
                        compact = compact,
                        query = query,
                        onQueryChange = viewMode::setQuery,
                        focusRequester = focusRequester,
                        onSubmit = {
                            if (query.isNotBlank()) {
                                keyEvent(
                                    backStack = backStack,
                                    editTextText = query,
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

                    when (val newState = uiState) {
                        UiState.Idle -> {
                            SearchEmptyState(
                                title = "Start searching",
                                message = "Look up videos, paste a YouTube link, or open something from recent history."
                            )
                        }

                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        is UiState.Error -> {
                            SearchEmptyState(
                                title = "Couldn’t load history",
                                message = newState.message,
                                isError = true
                            )
                        }

                        UiState.Empty -> {
                            SearchEmptyState(
                                title = "No recent searches",
                                message = "Your recent searches will appear here after you search for something."
                            )
                        }

                        is UiState.Success -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "RECENT SEARCHES",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp)
                                )

                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = gridMinSize),
                                    contentPadding = PaddingValues(bottom = 96.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(
                                        items = newState.data,
                                        key = { it.id }
                                    ) { item ->
                                        HistoryItem(
                                            title = item.value,
                                            compact = compact,
                                            onDelete = { viewMode.deleteById(item.id) },
                                            onClick = { text ->
                                                viewMode.setQuery(text)

                                                backStack.add(ResultViewerPage(text))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
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
private fun SearchHero(
    compact: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onSubmit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.TravelExplore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Find media faster",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (compact) {
                            "Search videos or paste a link"
                        } else {
                            "Search by title, keyword, or paste a YouTube video or playlist URL"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            "Keywords or YouTube URL",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = { onQueryChange("") }
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
                        onSearch = { onSubmit() }
                    )
                )
            }
        }
    }
}

@Composable
private fun HistoryItem(
    title: String,
    compact: Boolean,
    onDelete: () -> Unit,
    onClick: (String) -> Unit
) {
    val showDeleteConfirm = retain { mutableStateOf(false) }

    Surface(
        onClick = { onClick(title) },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(
                start = 14.dp,
                end = 6.dp,
                top = if (compact) 10.dp else 12.dp,
                bottom = if (compact) 10.dp else 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { showDeleteConfirm.value = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm.value = false },
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Remove from history?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Do you want to delete \"$title\" from recent searches?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm.value = false
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm.value = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SearchEmptyState(
    title: String,
    message: String,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 28.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isError) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f)
                },
                modifier = Modifier.size(112.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Error else Icons.Default.Search,
                        contentDescription = null,
                        tint = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PlayListDownloadDialog(
    url: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Download playlist?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = "https://img.youtube.com/vi/${extractPlaylistId(url)}/0.jpg",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Would you like to start downloading all videos in this playlist?",
                    textAlign = TextAlign.Center
                )
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
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
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


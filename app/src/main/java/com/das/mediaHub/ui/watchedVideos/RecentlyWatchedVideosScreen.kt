package com.das.mediaHub.ui.watchedVideos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.MainApplication
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.data.repository.WatchHistoryRepository
import com.das.mediaHub.navigation.NavScreens.OnlineVideoPlayer
import com.das.mediaHub.navigation.NavScreens.Saved
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.components.EmptyStateView
import com.das.mediaHub.ui.components.ErrorStateView
import com.das.mediaHub.ui.components.LibraryVideoItem
import com.das.mediaHub.ui.components.LoadingStateView

@Composable
fun RecentlyWatchedVideosScreen(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val app = context.applicationContext as MainApplication
    val dbHelper = retain {
        WatchHistoryRepository(app.appDatabase.historyDatabase.watchHistoryDao())
    }

    val viewModel = viewModel(
        modelClass = WatchedVideosViewModel::class.java.kotlin,
        factory = viewModelFactory {
            initializer {
                WatchedVideosViewModel(dbHelper)
            }
        }
    )
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val uiState by viewModel.savedListState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }
    val positionProvider = rememberTooltipPositionProvider(
        positioning = TooltipAnchorPosition.Below
    )

    Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    actions = {
                        TooltipBox(
                            modifier = Modifier,
                            positionProvider = positionProvider,
                            tooltip = {
                                PlainTooltip {
                                    Text(text = "Saved videos")
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = { backStack.add(Saved) }) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = "Saved videos",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            "Watch History",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (val newState = uiState) {
                    UiState.Loading -> {
                        item {
                            LoadingStateView(
                                title = "Loading watch history",
                                message = "Your recently watched videos will appear in a moment."
                            )
                        }
                    }

                    is UiState.Error -> {
                        item {
                            ErrorStateView(
                                message = newState.message,
                                title = "Couldn't load watch history",
                                buttonText = "Reload",
                                onRetry = {
                                    viewModel.fetchData()
                                }
                            )
                        }
                    }

                    UiState.Empty -> {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.History,
                                title = "Your history is empty",
                                message = "Videos you watch will appear here."
                            )
                        }
                    }

                    is UiState.Success -> {
                        items(newState.data, key = { it.watchUrl }) { searchItem ->
                            LibraryVideoItem(
                                selectedItem = searchItem,
                                onRemoveFromHistory = {
                                    viewModel.deleteVideo(searchItem.watchUrl)
                                },
                                onClickListListener = {
                                    onClickListListener(
                                        selectedId = searchItem.watchUrl,
                                        controller = backStack
                                    )
                                }
                            )
                        }

                    }

                    else -> Unit

                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
}




private fun onClickListListener(
    selectedId: String,
    controller: NavBackStack<NavKey>
) {
    try {
        controller.add(OnlineVideoPlayer(videoId = selectedId))
    } catch (e: Exception) {
        showNotificationDialog = TopPopUp(
            message = "Error: ${e.message}",
            icon = Icons.Default.VideoLibrary,
            loading = false
        )
    }
}

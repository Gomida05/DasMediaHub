package com.das.mediaHub.ui.watch_later

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.das.mediaHub.data.repository.FavoritesRepository
import com.das.mediaHub.navigation.NavScreens
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.components.EmptyStateView
import com.das.mediaHub.ui.components.ErrorStateView
import com.das.mediaHub.ui.components.LibraryVideoItem
import com.das.mediaHub.ui.components.LoadingStateView

@Composable
fun SavedVideosScreen(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val app = context.applicationContext as MainApplication

    val dbHelper = retain {
        FavoritesRepository(app.appDatabase.favoritesDatabase.favoritesDao())
    }
    val viewModel = viewModel(
        modelClass = SavedVideosViewModel::class.java.kotlin,
        factory = viewModelFactory {
            initializer {
                SavedVideosViewModel(dbHelper)
            }
        }
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val videos by viewModel.searchResults.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchData()
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
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            "Watch Later",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            },
            contentWindowInsets = WindowInsets.safeContent
        ) { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (val newState = videos) {
                    UiState.Idle -> Unit
                    UiState.Loading -> {
                        item {
                            LoadingStateView(
                                title = "Loading saved videos",
                                message = "Your saved videos will appear in a moment."
                            )
                        }
                    }

                    is UiState.Error -> {
                        item {
                            ErrorStateView(
                                message = newState.message,
                                title = "Couldn't load the saved videos",
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
                                icon = Icons.Default.Bookmark,
                                title = "No saved videos",
                                message = "Save videos to watch them later."
                            )
                        }
                    }

                    is UiState.Success -> {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(newState.data, key = { it.watchUrl }) { video ->
                            LibraryVideoItem(
                                selectedItem = video,
                                onRemoveFromHistory = {
                                    viewModel.deleteVideo(video.watchUrl)
                                },
                                onClickListListener = {
                                    onClickListListener(
                                        selectedId = video.watchUrl,
                                        backStack = backStack
                                    )
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}



private fun onClickListListener(
    selectedId: String,
    backStack: NavBackStack<NavKey>
) {
    try {

        backStack.add(NavScreens.OnlineVideoPlayer(videoId = selectedId))
    } catch (e: Exception) {
        showNotificationDialog = TopPopUp(
            message = "Error: ${e.message}",
            icon = Icons.Default.Bookmark,
            loading = false
        )
    }
}

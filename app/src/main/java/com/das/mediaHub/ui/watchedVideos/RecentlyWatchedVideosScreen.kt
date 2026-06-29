package com.das.mediaHub.ui.watchedVideos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.data.model.interfaces.UiState
import com.das.mediaHub.navigation.AppBackStack
import com.das.mediaHub.navigation.Destination
import com.das.mediaHub.navigation.Destination.OnlineVideoPlayer
import com.das.mediaHub.services.media.online.OnlineBackgroundPlayer.Companion.playAudioFromUrl
import com.das.mediaHub.ui.components.CustomTopAppBar
import com.das.mediaHub.ui.components.EmptyStateView
import com.das.mediaHub.ui.components.ErrorStateView
import com.das.mediaHub.ui.components.LibraryVideoItem
import com.das.mediaHub.ui.components.dialogs.ActionMenuItem
import com.das.mediaHub.ui.notification.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.players.videoPlayer.ActionDialogState
import com.das.mediaHub.ui.players.videoPlayer.ActionStatusDialog
import com.das.mediaHub.ui.players.videoPlayer.components.CustomLayouts.SkeletonSuggestionLoadingLayout

@Composable
fun RecentlyWatchedVideosScreen(backStack: AppBackStack) {

    val viewModel = hiltViewModel<WatchedVideosViewModel>()
    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching = retain { mutableStateOf(false) }
    val showMenu = retain { mutableStateOf(false) }
    val lazyGridState = rememberLazyGridState()
    val focusRequester = retain { FocusRequester() }
    var dialogState by retain { mutableStateOf<ActionDialogState>(ActionDialogState.Idle) }


    LaunchedEffect(isSearching.value) {
        if (isSearching.value) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            RecentlyWatchedTopAppBar(
                isSearching = isSearching.value,
                searchQuery = searchQuery,
                showMenu = showMenu.value,
                focusRequester = focusRequester,
                scrollBehavior = scrollBehavior,
                onSearchVideos = viewModel::searchVideos,
                onOpenMenu = { showMenu.value = it },
                onSearchModeChange = { isSearching.value = it },
                navigateToSaved = { backStack.add(Destination.Saved) },
                onClearAll = viewModel::clearAllVideos
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            state = lazyGridState,
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (val newState = uiState) {
                UiState.Loading, UiState.Idle -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            SkeletonSuggestionLoadingLayout(true)
                        }
                    }
                }

                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ErrorStateView(
                            message = newState.message,
                            title = "Couldn't find recently watched video"
                        )
                    }
                }

                UiState.Empty -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateView(
                            icon = Icons.Default.History,
                            title = "Your history is empty",
                            message = "Videos you watch will appear here."
                        )
                    }
                }

                is UiState.Success -> {
                    items(newState.data, key = { it.videoId }) { searchItem ->
                        LibraryVideoItem(
                            selectedItem = searchItem,
                            onRemoveFromHistory = {
                                viewModel.deleteVideo(searchItem.videoId)
                            },
                            onPlayItBackground = {
                                viewModel.loadStreamUrl(
                                    mediaItem = searchItem,
                                    onStart = {
                                        dialogState = ActionDialogState.Loading
                                    },
                                    onSuccess = { streamResult ->
                                        dialogState = ActionDialogState.Idle

                                        if (streamResult.audioUrl.isBlank()) {
                                            dialogState =
                                                ActionDialogState.Error("This video can’t be played in the background right now.")
                                            return@loadStreamUrl
                                        }

                                        context.playAudioFromUrl(
                                            audioUrl = streamResult.audioUrl,
                                            selectedItem = streamResult
                                        )
                                    },
                                    onFailure = {
                                        dialogState = ActionDialogState.Error(
                                            it.message ?: ""
                                        )

                                    }
                                )
                            }
                        ) {
                            onClickListListener(
                                selectedId = searchItem.videoId,
                                controller = backStack
                            )
                        }
                    }

                }

            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        ActionStatusDialog(dialogState) {
            dialogState = ActionDialogState.Idle
        }
    }
}


@Composable
fun RecentlyWatchedTopAppBar(
    isSearching: Boolean,
    searchQuery: String,
    showMenu: Boolean,
    focusRequester: FocusRequester,
    scrollBehavior: TopAppBarScrollBehavior,
    onSearchVideos: (String) -> Unit,
    onOpenMenu: (Boolean) -> Unit,
    onSearchModeChange: (Boolean) -> Unit,
    navigateToSaved: () -> Unit,
    onClearAll: () -> Unit
) {
    CustomTopAppBar(
        title = "History",
        isSearching = isSearching,
        searchQuery = searchQuery,
        focusRequester = focusRequester,
        scrollBehavior = scrollBehavior,
        onSearchModeChange = onSearchModeChange,
        onSearchVideos = onSearchVideos,
        onOpenMenu = onOpenMenu
    ){
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { onOpenMenu(false) },
            offset = DpOffset(x = 0.dp, y = 8.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 12.dp,
            shadowElevation = 18.dp,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(24.dp)
                )
                .border(
                    BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    RoundedCornerShape(24.dp)
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                ActionMenuItem(
                    Icons.Default.Save,
                    title = "Watch Later",
                    subtitle = "See all saved videos",
                    onClick = {
                        onOpenMenu(false)
                        navigateToSaved()
                    }
                )
                ActionMenuItem(
                    Icons.Default.Delete,
                    title = "Clear all",
                    subtitle = "delete all videos",
                    onClick = {
                        onOpenMenu(false)
//                        onClearAll()
                    }
                )


            }

        }
    }
}


private fun onClickListListener(
    selectedId: String,
    controller: AppBackStack
) {
    try {
        controller.add(OnlineVideoPlayer(videoId = selectedId))
    } catch (e: Exception) {
        showNotificationDialog = TopPopUp(
            message = ErrorMapper.map(e),
            icon = Icons.Default.VideoLibrary,
            loading = false
        )
    }
}


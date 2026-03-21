package com.das.mediaHub

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.core.content.ContextCompat
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.NavScreens.Downloaded
import com.das.mediaHub.NavScreens.Home
import com.das.mediaHub.NavScreens.RecentlyWatched
import com.das.mediaHub.NavScreens.Searcher
import com.das.mediaHub.NavScreens.Setting
import com.das.mediaHub.NavScreens.VideoViewer
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.model.BottomNavItem.Companion.rememberBottomNavigationItems
import com.das.mediaHub.services.AudioServiceFromUrl
import com.das.mediaHub.ui.players.videoPlayer.state.VideoUiState
import com.das.python.YouTuber.extractPlaylistId
import com.das.python.YouTuber.isValidYouTubePlaylistUrl
import com.das.python.YouTuber.isValidYoutubeURL
import com.das.python.YouTuber.youtubeExtractor
import com.das.python.data.model.ItemsStreamUrlsForMediaItemData
import com.das.python.data.model.VideosListData
import com.das.python.data.model.searcher.Video

internal object OnLaunchComponents {

    fun Context.openCustomTab(url: Uri) {
        val intent = CustomTabsIntent.Builder()
            .build()
        intent.launchUrl(this, url)
    }

    fun Context.playAudioFromUrl(
        audioUrl: String,
        selectedItem: ItemsStreamUrlsForMediaItemData
    ) {
        startAudioService(
            videoId = selectedItem.videoId,
            audioUrl = audioUrl,
            title = selectedItem.title,
            channelName = selectedItem.channelName,
            views = selectedItem.views,
            date = selectedItem.dateOfVideo,
            duration = selectedItem.duration
        )
    }

    fun Context.playAudioFromUrl(
        audioUrl: String,
        selectedItem: VideosListData
    ) {
        startAudioService(
            videoId = selectedItem.videoId,
            audioUrl = audioUrl,
            title = selectedItem.title,
            channelName = selectedItem.channelName,
            views = selectedItem.views,
            date = selectedItem.dateOfVideo,
            duration = selectedItem.duration
        )
    }

    fun Context.playAudioFromUrl(
        id: String,
        audioUrl: String,
        selectedItem: VideoUiState
    ) {
        startAudioService(
            videoId = id,
            audioUrl = audioUrl,
            title = selectedItem.title,
            channelName = selectedItem.channelName,
            views = selectedItem.views,
            date = selectedItem.date,
            duration = selectedItem.duration
        )
    }

    @Composable
    fun BottomNavItems(backStack: NavBackStack<NavKey>) {
        val currentRoute = backStack.lastOrNull()
        val bottomNavigationItems = rememberBottomNavigationItems()
        val positionProvider = rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above
        )
        if (currentRoute in listOf(Home, RecentlyWatched, Setting)) {
            NavigationBar(
                windowInsets = NavigationBarDefaults.windowInsets,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12))
            ) {
                bottomNavigationItems.forEach { items ->
                    key(items.key) {
                        NavigationBarItem(
                            selected = currentRoute == items.key,
                            onClick = {
                                if (currentRoute != items.key) {
                                    backStack.add(items.key)
                                }
                            },
                            icon = {
                                TooltipBox(
                                    positionProvider = positionProvider,
                                    tooltip = {
                                        PlainTooltip {
                                            Text(text = items.title)
                                        }
                                    },
                                    state = rememberTooltipState()
                                ) {
                                    Icon(
                                        imageVector = if (currentRoute == items.key) items.selectedIcon else items.unselectedIcon,
                                        contentDescription = items.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = items.title,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    fun NavBackStack<NavKey>.newTextIntent(
        sharedText: String
    ) {
        if (sharedText.isValidYoutubeURL()) {
            val videoId = sharedText.youtubeExtractor()
            add(
                VideoViewer(
                    Video(id = videoId.toString())
                )
            )

        } else if (sharedText.isValidYouTubePlaylistUrl()) {

            add(
                VideoViewer(
                    Video(id = extractPlaylistId(sharedText).toString())
                )
            )

        } else if (sharedText.startsWith("DownloadsPageFr")) {
            add(Downloaded)
        } else {
            add(Searcher(sharedText))
        }
    }

    private fun Context.startAudioService(
        videoId: String,
        audioUrl: String,
        title: String?,
        channelName: String?,
        views: String?,
        date: String?,
        duration: String?
    ) {
        val playIntent = Intent(this, AudioServiceFromUrl::class.java).apply {
            action = ACTION_START
            putExtra("videoId", videoId)
            putExtra("media_url", audioUrl)
            putExtra("title", title)
            putExtra("channelName", channelName)
            putExtra("viewNumber", views)
            putExtra("videoDate", date)
            putExtra("duration", duration)
        }
        ContextCompat.startForegroundService(this, playIntent)
    }

}


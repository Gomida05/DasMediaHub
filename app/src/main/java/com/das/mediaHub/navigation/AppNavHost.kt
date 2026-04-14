package com.das.mediaHub.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.das.mediaHub.navigation.NavScreens.AboutDasMediaHub
import com.das.mediaHub.navigation.NavScreens.PrivacyPolicy
import com.das.mediaHub.navigation.NavScreens.Help
import com.das.mediaHub.navigation.NavScreens.Downloaded
import com.das.mediaHub.navigation.NavScreens.DownloadsPage
import com.das.mediaHub.navigation.NavScreens.FeedbackScreen
import com.das.mediaHub.navigation.NavScreens.Home
import com.das.mediaHub.navigation.NavScreens.Instagram
import com.das.mediaHub.navigation.NavScreens.LocalVideoPlayer
import com.das.mediaHub.navigation.NavScreens.OnlineVideoPlayer
import com.das.mediaHub.navigation.NavScreens.RecentlyWatched
import com.das.mediaHub.navigation.NavScreens.Saved
import com.das.mediaHub.navigation.NavScreens.Searcher
import com.das.mediaHub.navigation.NavScreens.Setting
import com.das.mediaHub.navigation.NavScreens.TikTok
import com.das.mediaHub.ui.downloaded.DownloadedScreen
import com.das.mediaHub.ui.downloads.DownloadingComposable
import com.das.mediaHub.ui.home.HomePageScreen
import com.das.mediaHub.ui.home.PageNotFound
import com.das.mediaHub.ui.instagram.InstagramScreen
import com.das.mediaHub.ui.players.videoPlayer.OnlineVideoPlayerScreen
import com.das.mediaHub.ui.players.videoPlayerLocally.LocalVideoPlayer
import com.das.mediaHub.ui.result.ResultViewerPage
import com.das.mediaHub.ui.search.SearchPageCompose
import com.das.mediaHub.ui.settings.AboutDasMediaHub
import com.das.mediaHub.ui.settings.report.UserFeedbackScreen
import com.das.mediaHub.ui.settings.HelpScreen
import com.das.mediaHub.ui.settings.PrivacyPolicyScreen
import com.das.mediaHub.ui.settings.SettingsScreen
import com.das.mediaHub.ui.watch_later.SavedVideosScreen
import com.das.mediaHub.ui.tiktok.TikTokComposable
import com.das.mediaHub.ui.watchedVideos.RecentlyWatchedVideosScreen

@Composable
fun AppNavHost(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier
) {
    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        modifier = modifier.fillMaxSize()
    ) { key ->

        when (key) {
            is Home -> NavEntry(key = key) {
                HomePageScreen { backStack.add(it) }
            }

            is RecentlyWatched -> NavEntry(key = key) {
                RecentlyWatchedVideosScreen(backStack)
            }

            is Setting -> NavEntry(key = key) {
                SettingsScreen { backStack.add(it) }
            }

            is OnlineVideoPlayer -> NavEntry(key = key) {
                OnlineVideoPlayerScreen(backStack, key.videoId)
            }

            is NavScreens.ResultViewerPage -> NavEntry(key = key) {
                ResultViewerPage(backStack, key.value)
            }

            is DownloadsPage -> NavEntry(key = key) {
                DownloadingComposable(backStack)
            }

            is Downloaded -> NavEntry(key = key) {
                DownloadedScreen(backStack)
            }

            is Searcher -> NavEntry(key = key) {
                SearchPageCompose(backStack, key.text)
            }

            is LocalVideoPlayer -> NavEntry(key = key) {
                LocalVideoPlayer(videoUri = key.uri)
            }

            is Saved -> NavEntry(key = key) {
                SavedVideosScreen(backStack)
            }

            is TikTok -> NavEntry(key = key) {
                TikTokComposable { backStack.removeLastOrNull() }
            }

            is Instagram -> NavEntry(key = key) {
                InstagramScreen { backStack.removeLastOrNull() }
            }

            is FeedbackScreen -> NavEntry(key = key) {
                UserFeedbackScreen()
            }

            is AboutDasMediaHub -> NavEntry(key = key) {
                AboutDasMediaHub(backStack)
            }

            is PrivacyPolicy -> NavEntry(key = key) {
                PrivacyPolicyScreen(backStack)
            }

            is Help -> NavEntry(key = key) {
                HelpScreen(backStack)
            }

            else -> NavEntry(key = key) {
                PageNotFound(backStack)
            }
        }
    }
}
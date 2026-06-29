package com.das.mediaHub.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.das.mediaHub.navigation.Destination.AboutDasMediaHub
import com.das.mediaHub.navigation.Destination.Downloaded
import com.das.mediaHub.navigation.Destination.DownloadsPage
import com.das.mediaHub.navigation.Destination.FeedbackScreen
import com.das.mediaHub.navigation.Destination.Help
import com.das.mediaHub.navigation.Destination.Home
import com.das.mediaHub.navigation.Destination.LocalVideoPlayer
import com.das.mediaHub.navigation.Destination.OnlineVideoPlayer
import com.das.mediaHub.navigation.Destination.PrivacyPolicy
import com.das.mediaHub.navigation.Destination.RecentlyWatched
import com.das.mediaHub.navigation.Destination.Saved
import com.das.mediaHub.navigation.Destination.Searcher
import com.das.mediaHub.navigation.Destination.Setting
import com.das.mediaHub.navigation.Destination.SocialDownloader
import com.das.mediaHub.ui.downloaded.DownloadedScreen
import com.das.mediaHub.ui.downloads.DownloadingScreen
import com.das.mediaHub.ui.home.HomePageScreen
import com.das.mediaHub.ui.home.PageNotFound
import com.das.mediaHub.ui.players.videoPlayer.VideoPlayerScreen
import com.das.mediaHub.ui.players.videoPlayerLocally.LocalVideoPlayer
import com.das.mediaHub.ui.result.ResultScreen
import com.das.mediaHub.ui.search.SearchScreen
import com.das.mediaHub.ui.settings.AboutDasMediaHub
import com.das.mediaHub.ui.settings.HelpScreen
import com.das.mediaHub.ui.settings.PrivacyPolicyScreen
import com.das.mediaHub.ui.settings.SettingsScreen
import com.das.mediaHub.ui.settings.download.DownloadSettingScreen
import com.das.mediaHub.ui.settings.report.UserFeedbackScreen
import com.das.mediaHub.ui.social.SocialDownloaderScreen
import com.das.mediaHub.ui.watch_later.SavedVideosScreen
import com.das.mediaHub.ui.watchedVideos.RecentlyWatchedVideosScreen

typealias AppBackStack = NavBackStack<NavKey>

@Composable
fun AppNavHost(
    backStack: AppBackStack,
    paddingValues: PaddingValues,
    onShowMainUpdateDialog: () -> Unit
) {


    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) { key ->

        when (key) {
            is Home -> NavEntry(key = key) {
                HomePageScreen(navigate = backStack::add)
            }

            is RecentlyWatched -> NavEntry(key = key) {
                RecentlyWatchedVideosScreen(backStack)
            }

            is Setting -> NavEntry(key = key) {
                SettingsScreen(
                    add = backStack::add,
                    onShowMainUpdateDialog = onShowMainUpdateDialog
                )
            }

            is OnlineVideoPlayer -> NavEntry(key = key) {
                VideoPlayerScreen(
                    videoID = key.videoId,
                    onNavigateUp = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            is Destination.ResultRoute -> NavEntry(key = key) {
                ResultScreen(backStack, key.value)
            }

            is DownloadsPage -> NavEntry(key = key) {
                DownloadingScreen(backStack)
            }

            is Downloaded -> NavEntry(key = key) {
                DownloadedScreen(backStack)
            }

            is Searcher -> NavEntry(key = key) {
                SearchScreen(backStack, key.text)
            }

            is LocalVideoPlayer -> NavEntry(key = key) {
                LocalVideoPlayer(videoUri = key.uri)
            }

            is Saved -> NavEntry(key = key) {
                SavedVideosScreen(backStack)
            }

            is SocialDownloader -> NavEntry(key = key) {
                SocialDownloaderScreen(newUrl = key.newUrl) { backStack.removeLastOrNull() }
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

            Destination.DownloadSetting -> NavEntry(key = key) {
                DownloadSettingScreen {
                    backStack.removeLastOrNull()
                }
            }

            else -> NavEntry(key = key) {
                PageNotFound(backStack)
            }
        }
    }
}
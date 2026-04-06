package com.das.mediaHub.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavScreens: NavKey {
    @Serializable
    data object Home: NavScreens
    @Serializable
    data class Searcher(val text: String) : NavScreens

    @Serializable
    data class OnlineVideoPlayer(val videoId: String) : NavScreens
    @Serializable
    data class ResultViewerPage(val value: String) : NavScreens
    @Serializable
    data object RecentlyWatched : NavScreens
    @Serializable
    data object Saved : NavScreens
    @Serializable
    data object DownloadsPage : NavScreens

    @Serializable
    data object Downloaded : NavScreens

    @Serializable
    data class LocalVideoPlayer(val uri: String) : NavScreens

    @Serializable
    data object Setting : NavScreens
    @Serializable
    data object FeedbackScreen: NavScreens

    @Serializable
    data object TikTok : NavScreens

    @Serializable
    data object Instagram : NavScreens

    @Serializable
    data object AboutDasMediaHub: NavScreens
}
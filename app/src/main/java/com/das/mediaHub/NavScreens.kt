package com.das.mediaHub

import androidx.navigation3.runtime.NavKey
import com.das.python.data.model.searcher.Video
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavScreens: NavKey {
    @Serializable
    data object Home: NavScreens
    @Serializable
    data class Searcher(val text: String) : NavScreens

    @Serializable
    data class VideoViewer(val data: Video) : NavScreens
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
    data class ExoPlayerUI(val uri: String) : NavScreens

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
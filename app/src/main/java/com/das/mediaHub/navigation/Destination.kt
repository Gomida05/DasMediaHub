package com.das.mediaHub.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Destination : NavKey {

    @Serializable
    data object Home : Destination

    @Serializable
    data class Searcher(val text: String) : Destination

    @Serializable
    data class OnlineVideoPlayer(val videoId: String) : Destination

    @Serializable
    data class ResultRoute(val value: String) : Destination

    @Serializable
    data object RecentlyWatched : Destination

    @Serializable
    data object Saved : Destination

    @Serializable
    data object DownloadsPage : Destination

    @Serializable
    data object Downloaded : Destination

    @Serializable
    data class LocalVideoPlayer(val uri: String) : Destination

    @Serializable
    data object Setting : Destination

    @Serializable
    data object FeedbackScreen : Destination

    @Serializable
    data class SocialDownloader(val newUrl: String? = null) : Destination

    @Serializable
    data object AboutDasMediaHub : Destination

    @Serializable
    data object PrivacyPolicy : Destination

    @Serializable
    data object Help : Destination

    @Serializable
    data object DownloadSetting: Destination
}
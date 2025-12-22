package com.das.mediaHub

import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.data.model.searcher.Video
import kotlinx.serialization.Serializable

@Serializable
sealed class NavScreens: NavKey {
    @Serializable
    data object WelcomePage : NavScreens()
    @Serializable
    data object Home: NavScreens()
    @Serializable
    data class Searcher(val text: String) : NavScreens()


    @Serializable
    data class VideoViewer(val data: Video) : NavScreens()
    @Serializable
    data class ResultViewerPage(val value: String) : NavScreens()
    @Serializable
    data object RecentlyWatched : NavScreens()
    @Serializable
    data object Saved : NavScreens()
    @Serializable
    data object Downloads : NavScreens()
    @Serializable
    data object UserSettings : NavScreens()
    @Serializable
    data class ExoPlayerUI(val uri: String) : NavScreens()

    @Serializable
    data object Setting : NavScreens()
    @Serializable
    data object FeedbackScreen: NavScreens()

    @Serializable
    data object SignInPage : NavScreens()
    @Serializable
    data object SignUpPage : NavScreens()

    @Serializable
    data object AccountSetting : NavScreens()
    @Serializable
    data object ChangePassword : NavScreens()
}
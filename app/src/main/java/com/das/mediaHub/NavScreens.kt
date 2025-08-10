package com.das.mediaHub

sealed class NavScreens(val route: String) {
    data object WelcomePage : NavScreens("Welcome Page")
    data object Home: NavScreens("Home")
    data object Searcher : NavScreens("searcher")
    data object VideoViewer : NavScreens("video viewer")
    data object ResultViewerPage : NavScreens("ResultViewerPage")
    data object RecentlyWatched : NavScreens("Recently Watched")
    data object Saved : NavScreens("saved")
    data object Downloads : NavScreens("Downloads")
    data object UserSettings : NavScreens("user Setting")
    data object ExoPlayerUI : NavScreens("ExoPlayerUI")

    data object Setting : NavScreens("Setting")
    data object FeedbackScreen: NavScreens("Feedback")

    data object SignInPage : NavScreens("sign in page")
    data object SignUpPage : NavScreens("sign up page")

    data object AccountSetting : NavScreens("User account settings page")
    data object ChangePassword : NavScreens("change_password")
}
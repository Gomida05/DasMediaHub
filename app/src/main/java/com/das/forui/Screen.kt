package com.das.forui

sealed class Screen(val route: String) {

    data object Home: Screen("Home")
    data object Searcher : Screen("searcher")
    data object VideoViewer : Screen("video viewer")
    data object ResultViewerPage : Screen("ResultViewerPage")
    data object RecentlyWatched : Screen("Recently Watched")
    data object Saved : Screen("saved")
    data object Downloads : Screen("Downloads")
    data object UserSettings : Screen("user Setting")
    data object ExoPlayerUI : Screen("ExoPlayerUI")

    data object Setting : Screen("Setting")

    data object LoginPage1 : Screen("login page1")
    data object LoginPage2 : Screen("login page2")
    data object SignUpPage : Screen("signup page")
}
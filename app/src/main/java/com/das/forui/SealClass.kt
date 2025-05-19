package com.das.forui

sealed class Screen(val route: String) {
    data object Saved : Screen("saved")
    data object UserSettings : Screen("user Setting")
}
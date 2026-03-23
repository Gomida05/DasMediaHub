package com.das.mediaHub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.navigation.NavScreens

@Stable
data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val key: NavKey
) {
    internal companion object {
        @Composable
        fun rememberBottomNavigationItems(): List<BottomNavItem> {
            return retain {
                listOf(
                    BottomNavItem(
                        title = "Home",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        key = NavScreens.Home
                    ),
                    BottomNavItem(
                        title = "Recently Watched",
                        selectedIcon = Icons.Filled.WatchLater,
                        unselectedIcon = Icons.Outlined.WatchLater,
                        key = NavScreens.RecentlyWatched
                    ),
                    BottomNavItem(
                        title = "Setting",
                        selectedIcon = Icons.Filled.Settings,
                        unselectedIcon = Icons.Outlined.Settings,
                        key = NavScreens.Setting
                    )
                )
            }
        }
    }
}
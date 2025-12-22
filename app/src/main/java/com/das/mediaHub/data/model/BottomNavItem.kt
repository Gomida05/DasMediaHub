package com.das.mediaHub.data.model

import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.NavScreens.Home
import com.das.mediaHub.NavScreens.RecentlyWatched
import com.das.mediaHub.NavScreens.Setting

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
            return remember {
                listOf(
                    BottomNavItem(
                        title = "Home",
                        selectedIcon = Filled.Home,
                        unselectedIcon = Outlined.Home,
                        key = Home
                    ),
                    BottomNavItem(
                        title = "Recently Watched",
                        selectedIcon = Filled.WatchLater,
                        unselectedIcon = Outlined.WatchLater,
                        key = RecentlyWatched
                    ),
                    BottomNavItem(
                        title = "Setting",
                        selectedIcon = Filled.Settings,
                        unselectedIcon = Outlined.Settings,
                        key = Setting
                    )
                )
            }
        }
    }
}

package com.das.mediaHub.navigation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.navigation.NavScreens.Home
import com.das.mediaHub.navigation.NavScreens.RecentlyWatched
import com.das.mediaHub.navigation.NavScreens.Setting

@Composable
fun BottomNavItems(backStack: NavBackStack<NavKey>) {
    val currentRoute = backStack.lastOrNull()
    val bottomNavigationItems = BottomNavItem.rememberBottomNavigationItems()
    val positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
        positioning = TooltipAnchorPosition.Above
    )
    if (currentRoute in listOf(Home, RecentlyWatched, Setting)) {

        NavigationBar(
            windowInsets = NavigationBarDefaults.windowInsets,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(12))
        ) {
            bottomNavigationItems.forEach { items ->
                key(items.key) {
                    NavigationBarItem(
                        selected = currentRoute == items.key,
                        onClick = {
                            if (currentRoute != items.key) {
                                backStack.add(items.key)
                            }
                        },
                        icon = {
                            TooltipBox(
                                positionProvider = positionProvider,
                                tooltip = {
                                    PlainTooltip {
                                        Text(text = items.title)
                                    }
                                },
                                state = rememberTooltipState()
                            ) {
                                Icon(
                                    imageVector = if (currentRoute == items.key) items.selectedIcon else items.unselectedIcon,
                                    contentDescription = items.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = items.title,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }
    }
}
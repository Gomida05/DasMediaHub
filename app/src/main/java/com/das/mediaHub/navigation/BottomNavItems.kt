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
import com.das.mediaHub.navigation.Destination.Home
import com.das.mediaHub.navigation.Destination.RecentlyWatched
import com.das.mediaHub.navigation.Destination.Setting

@Composable
fun BottomNavItems(
    backStack: AppBackStack
) {

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
                .clip(RoundedCornerShape(12))) {
            bottomNavigationItems.forEach { item ->
                key(item.key) {
                    NavigationBarItem(
                        selected = currentRoute == item.key,
                        onClick = {
                            if (currentRoute != item.key) {
                                backStack.add(item.key)
                            }
                        },
                        icon = {
                            TooltipBox(
                                positionProvider = positionProvider,
                                tooltip = {
                                    PlainTooltip {
                                        Text(text = item.title)
                                    }
                                },
                                state = rememberTooltipState()
                            ) {
                                Icon(
                                    imageVector = if (currentRoute == item.key) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = item.title,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }
    }
}

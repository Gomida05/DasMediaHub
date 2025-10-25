package com.das.mediaHub

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.das.mediaHub.NavScreens.Home
import com.das.mediaHub.NavScreens.RecentlyWatched
import com.das.mediaHub.NavScreens.Setting
import com.das.mediaHub.data.model.BottomNavItem
import com.google.firebase.auth.FirebaseAuth

internal object OnLaunchComponents {

    internal fun Context.openCustomTab(url: Uri) {
        val intent = CustomTabsIntent.Builder()
            .build()
        intent.launchUrl(this, url)
    }

    @Composable
    fun FirebaseAuth.rememberAuthState(): Boolean? {
        var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

        DisposableEffect(Unit) {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                isLoggedIn = user != null
            }
            addAuthStateListener(listener)
            onDispose { removeAuthStateListener(listener) }
        }

        return isLoggedIn
    }


    @Composable
    fun BottomNavItems(currentRoute: String?, navController: NavController) {
        val bottomNavigationItems = rememberBottomNavigationItems()

        if (currentRoute in listOf(Home.route, RecentlyWatched.route, Setting.route)) {
            NavigationBar(
                windowInsets = NavigationBarDefaults.windowInsets,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12))
            ) {
                bottomNavigationItems.forEachIndexed { _, items ->
                    NavigationBarItem(
                        selected = currentRoute == items.title,
                        onClick = {
                            if (currentRoute != items.title) {
                                navController.navigate(items.title) {
                                    // Avoid multiple copies of the same destination
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == items.title) items.selectedIcon else items.unselectedIcon,
                                contentDescription = items.title
                            )
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




    @Composable
    private fun rememberBottomNavigationItems(): List<BottomNavItem> {
        return remember {
            listOf(
                BottomNavItem(
                    title = Home.route,
                    selectedIcon = Filled.Home,
                    unselectedIcon = Outlined.Home
                ),
                BottomNavItem(
                    title = RecentlyWatched.route,
                    selectedIcon = Filled.WatchLater,
                    unselectedIcon = Outlined.WatchLater
                ),
                BottomNavItem(
                    title = Setting.route,
                    selectedIcon = Filled.Settings,
                    unselectedIcon = Outlined.Settings
                )
            )
        }
    }

}
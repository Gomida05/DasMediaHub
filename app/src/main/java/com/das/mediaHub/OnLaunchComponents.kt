package com.das.mediaHub

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.NavScreens.Home
import com.das.mediaHub.NavScreens.RecentlyWatched
import com.das.mediaHub.NavScreens.Setting
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.model.BottomNavItem.Companion.rememberBottomNavigationItems
import com.das.mediaHub.data.model.ItemsStreamUrlsForMediaItemData
import com.das.mediaHub.services.AudioServiceFromUrl

internal object OnLaunchComponents {

    fun Context.openCustomTab(url: Uri) {
        val intent = CustomTabsIntent.Builder()
            .build()
        intent.launchUrl(this, url)
    }

    fun Context.playAudioFromUrl(audioUrl: String, selectedItem: ItemsStreamUrlsForMediaItemData) {
        val playIntent = Intent(this, AudioServiceFromUrl::class.java).apply {
            action = ACTION_START
            putExtra("videoId", selectedItem.videoId)
            putExtra("media_url", audioUrl)
            putExtra("title", selectedItem.title)
            putExtra("channelName", selectedItem.channelName)
            putExtra("viewNumber", selectedItem.views)
            putExtra("videoDate", selectedItem.dateOfVideo)
            putExtra("duration", selectedItem.duration)
        }
        ContextCompat.startForegroundService(this, playIntent)
    }

    @Composable
    fun BottomNavItems(currentRoute: NavKey?, backStack: NavBackStack<NavKey>) {
        val bottomNavigationItems = rememberBottomNavigationItems()

        if (currentRoute in listOf(Home, RecentlyWatched, Setting)) {
            NavigationBar(
                windowInsets = NavigationBarDefaults.windowInsets,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12))
            ) {
                bottomNavigationItems.forEachIndexed { _, items ->
                    NavigationBarItem(
                        selected = currentRoute == items.key,
                        onClick = {
                            if (currentRoute != items.key) {
                                backStack.add(items.key)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == items.key) items.selectedIcon else items.unselectedIcon,
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
}


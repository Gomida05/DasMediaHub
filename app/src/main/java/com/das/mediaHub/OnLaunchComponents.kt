package com.das.mediaHub

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.NavScreens.Home
import com.das.mediaHub.NavScreens.RecentlyWatched
import com.das.mediaHub.NavScreens.Setting
import com.das.mediaHub.data.constants.Action.ACTION_START
import com.das.mediaHub.data.model.BottomNavItem.Companion.rememberBottomNavigationItems
import com.das.mediaHub.data.model.ItemsStreamUrlsForMediaItemData
import com.das.mediaHub.data.model.MediaData
import com.das.mediaHub.downloader.DownloaderClass
import com.das.mediaHub.python.YouTuber.getAudioStreamUrl
import com.das.mediaHub.python.YouTuber.getVideoStreamUrl
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
        startService(playIntent)
    }

    suspend fun Context.DownloadMedia(data: MediaData, failed: (String) -> Unit) {
        val downloader = DownloaderClass(this)

        when (data.type) {
            true -> {
                getAudioStreamUrl(videoId = data.id,
                    onSuccess = {
                        downloader.downloadVideo(it, data.title)
                    }
                ) {
                    failed(it)
                }
            }

            false -> {
                getVideoStreamUrl(videoId = data.id,
                    onSuccess = {
                        downloader.downloadMusic(it, data.title)
                    }
                ) {
                    failed(it)
                }
            }
        }


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
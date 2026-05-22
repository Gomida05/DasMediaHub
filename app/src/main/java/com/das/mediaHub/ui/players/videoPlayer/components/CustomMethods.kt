package com.das.mediaHub.ui.players.videoPlayer.components

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.das.mediaHub.data.model.VideoUiModel
import com.das.python.data.model.VideosListData
import com.das.python.data.model.searcher.Video

/**
 * Utility object containing extension functions and helper methods
 * for video data conversion and UI manipulation (fullscreen, rotation, etc.).
 */
object CustomMethods {

    /**
     * Converts a [Video] object from the searcher model to a [VideosListData] model.
     *
     * Example usage:
     * ```kotlin
     * val videoData = video.toVideosListData()
     * ```
     *
     * @receiver The source [Video] object.
     * @return A new [VideosListData] instance.
     */
    fun Video.toVideosListData(): VideosListData {
        return VideosListData(
            videoId = id,
            title = title ?: "",
            views = viewCount?.short ?: "0",
            dateOfVideo = publishedTime ?: "",
            duration = channel?.name ?: "",
            channelName = duration ?: "0:00",
            channelThumbnailsUrl = channel?.thumbnails?.get(0)?.url ?: ""
        )
    }

    /**
     * Converts a [VideoUiModel] to a [VideosListData] model.
     *
     * Example usage:
     * ```kotlin
     * val videoData = uiModel.toVideosListData()
     * ```
     *
     * @receiver The source [VideoUiModel] object.
     * @return A new [VideosListData] instance.
     */
    fun VideoUiModel.toVideosListData(): VideosListData {
        return VideosListData(
            videoId = videoId,
            title = title,
            views = views,
            dateOfVideo = dateTime,
            duration = duration,
            channelName = channelName,
            channelThumbnailsUrl = channelThumbnail
        )
    }

    /**
     * Sets the activity to fullscreen by hiding/showing system bars.
     *
     * @receiver The current [Activity].
     * @param fullscreen True to enable fullscreen mode, false to disable.
     */
    private fun Activity.setFullscreen(fullscreen: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, !fullscreen)
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        if (fullscreen) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    /**
     * Rotates the screen to landscape when in fullscreen mode and manages system bar visibility.
     *
     * Example usage:
     * ```kotlin
     * activity.rotateScreen(true) // Switch to landscape and fullscreen
     * ```
     *
     * @receiver The current [Activity].
     * @param fullScreen True to rotate to landscape and hide system bars.
     */
    fun Activity.rotateScreen(fullScreen: Boolean) {
        requestedOrientation = if (fullScreen) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        setFullscreen(fullScreen || isInPictureInPictureMode)
    }

    /**
     * Opens a given URL using Chrome Custom Tabs.
     *
     * Example usage:
     * ```kotlin
     * context.openCustomTab(Uri.parse("https://www.youtube.com"))
     * ```
     *
     * @receiver The [Context] used to launch the intent.
     * @param url The [Uri] to open.
     */
    fun Context.openCustomTab(url: Uri) {
        val intent = CustomTabsIntent.Builder()
            .build()
        intent.launchUrl(this, url)
    }
}

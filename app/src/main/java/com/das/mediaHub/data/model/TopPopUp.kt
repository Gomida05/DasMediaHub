package com.das.mediaHub.data.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing the configuration for a top-of-screen popup notification.
 *
 * @property message The text message to display in the popup.
 * @property icon The [ImageVector] to display alongside the message.
 * @property loading Whether to show a loading indicator instead of the icon.
 *
 * Example usage:
 * ```kotlin
 * val popup = TopPopUp(
 *     message = "Download started",
 *     icon = Icons.Default.DownloadDone
 * )
 * ```
 */
data class TopPopUp(
    val message: String,
    val icon: ImageVector,
    val loading: Boolean = false
)

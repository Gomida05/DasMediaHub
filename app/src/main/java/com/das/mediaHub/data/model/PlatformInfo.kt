package com.das.mediaHub.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class that holds UI-related information for a specific social media platform.
 * Used to dynamically style dialogs and components based on the detected link type.
 *
 * @property name The display name of the platform (e.g., "YouTube", "Instagram").
 * @property icon The [ImageVector] representing the platform's logo.
 * @property color The brand-specific [Color] associated with the platform.
 */
data class PlatformInfo(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

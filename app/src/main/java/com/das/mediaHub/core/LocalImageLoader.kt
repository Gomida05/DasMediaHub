package com.das.mediaHub.core

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import coil.ImageLoader

/**
 * A [CompositionLocal] that provides a Coil [ImageLoader] instance specifically
 * configured for loading local media assets, such as thumbnails from downloaded
 * videos or local files.
 *
 * Example usage:
 * ```
 * val imageLoader = LocalImageLoader.current
 * AsyncImage(
 *     model = localVideoFile, // e.g., a File or path to a .mp4
 *     imageLoader = imageLoader,
 *     contentDescription = "Video Thumbnail"
 * )
 * ```
 */
val LocalImageLoader  = staticCompositionLocalOf<ImageLoader> {
    error("No ImageLoader provided")
}
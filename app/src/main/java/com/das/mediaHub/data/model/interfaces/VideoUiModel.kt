package com.das.mediaHub.data.model.interfaces

/**
 * Interface representing the UI data model for a video.
 *
 * This interface defines the common properties required to display a video 
 * in the UI, such as in lists, cards, or player views.
 *
 * Example usage:
 * ```kotlin
 * class MyVideoModel(
 *     override val videoId: String,
 *     override val title: String,
 *     // ... other properties
 * ) : VideoUiModel
 * ```
 */
interface VideoUiModel {
    /** Unique identifier for the video. */
    val videoId: String
    
    /** Title of the video. */
    val title: String
    
    /** Formatted view count (e.g., "1.2M views"). */
    val views: String
    
    /** Formatted upload date or relative time (e.g., "2 days ago"). */
    val dateTime: String
    
    /** Duration of the video (e.g., "10:05"). */
    val duration: String
    
    /** Name of the channel that uploaded the video. */
    val channelName: String
    
    /** URL of the video's thumbnail image. */
    val thumbnailUrl: String
    
    /** URL of the channel's thumbnail image. */
    val channelThumbnail: String
}

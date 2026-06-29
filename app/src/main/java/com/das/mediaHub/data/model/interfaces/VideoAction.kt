package com.das.mediaHub.data.model.interfaces

import com.das.downloader.data.model.download.DownloadType

/**
 * Sealed interface representing various actions that can be performed on a video.
 *
 * This is used to handle UI events such as favoriting, downloading, or sharing 
 * a video from various parts of the application.
 *
 * Example usage:
 * ```kotlin
 * fun onAction(action: VideoAction) {
 *     when (action) {
 *         is VideoAction.Share -> // Handle sharing
 *         is VideoAction.Download -> // Handle download with action.type
 *         // ...
 *     }
 * }
 * ```
 */
sealed interface VideoAction {
    /** Action to toggle the video in watch history. */
    object ToggleHistory : VideoAction
    
    /** 
     * Action to toggle the favorite status of a video.
     * @property insert True to add to favorites, false to remove.
     */
    data class ToggleFavorite(val insert: Boolean) : VideoAction
    
    /** 
     * Action to initiate a download for the video.
     * @property id The unique ID of the video to download.
     * @property title The title to use for the downloaded file.
     * @property type The type of download (e.g., Video, Music).
     */
    data class Download(val id: String, val title: String, val type: DownloadType) : VideoAction
    
    /** Action to share the video URL. */
    object Share: VideoAction
    
    /** Action to open the video in the official YouTube application. */
    object PlayInYoutube : VideoAction
    
    /** Action to play the video's audio in the background. */
    object PlayBackground: VideoAction
}

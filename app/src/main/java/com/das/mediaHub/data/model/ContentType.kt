package com.das.mediaHub.data.model

/**
 * Enum representing the types of media content supported by the application.
 *
 * @property extension The file extension associated with the content type.
 */
enum class ContentType(val extension: String) {
    /** Video content with .mp4 extension. */
    VIDEO(".mp4"),
    
    /** Audio content with .mp3 extension. */
    MUSIC(".mp3")
}

package com.das.mediaHub.data.constants

/**
 * Constants related to notification channel IDs or action strings 
 * specifically for foreground services.
 */
object Notifications {
    /** Action string for when the audio service notification is deleted. */
    const val AUDIO_SERVICE_FROM_URL_NOTIFICATION = "com.das.mediaHub.services.media.AudioServiceFromUrl.NOTIFICATION_DELETED"
    
    /** Action string for when the background player notification is deleted. */
    const val BACKGROUND_GROUND_PLAYER_NOTIFICATION = "com.das.mediaHub.services.BackgroundPlayer.NOTIFICATION_DELETED"
}

package com.das.mediaHub.data.constants

/**
 * Constants related to notification channel IDs or action strings 
 * specifically for foreground services.
 */
object Notifications {
    /** Action string for when the audio service notification is deleted. */
    const val AUDIO_SERVICE_FROM_URL_NOTIFICATION = "OnlineBackgroundPlayer.NOTIFICATION_DELETED"

    const val OPEN_IT_NOW = "OnlineBackgroundPlayer.OPEN_CURRENTLY"

    /** Action string for when the background player notification is deleted. */
    const val BACKGROUND_GROUND_PLAYER_NOTIFICATION = "BackgroundPlayer.NOTIFICATION_DELETED"
}

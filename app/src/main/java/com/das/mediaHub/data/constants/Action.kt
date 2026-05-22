package com.das.mediaHub.data.constants

/**
 * Constants representing Intent actions used for communication between components, 
 * particularly for controlling background media playback and handling notifications.
 */
object Action {

    /** Action to start background media playback. */
    const val ACTION_START = "com.das.mediaHub.START_BACKGROUND_MEDIA"
    
    /** Action to pause background media playback. */
    const val ACTION_PAUSE = "com.das.mediaHub.PAUSE_BACKGROUND_MEDIA"
    
    /** Action to stop and kill the background media service. */
    const val ACTION_KILL = "com.das.mediaHub.kill"
    
    /** Action triggered from a notification to add a video to "Watch Later". */
    const val ACTION_ADD_TO_WATCH_LATER = "com.das.mediaHub.ACTION_ADD_TO_WATCH_LATER"
}

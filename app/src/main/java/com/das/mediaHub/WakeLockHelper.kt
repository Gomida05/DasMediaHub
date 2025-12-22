package com.das.mediaHub

import android.app.Activity
import android.view.WindowManager

/**
 * Helper object for managing screen-related flags on an Activity.
 *
 * This utility provides extension functions to:
 * - Keep the screen awake while the Activity is in use
 * - Prevent the screen content from being captured (screenshots / screen recording)
 *
 * All functions operate directly on the Activity's window flags.
 */
internal object WakeLockHelper {

    /**
     * Keeps the device screen turned on while the Activity is in the foreground.
     *
     * This prevents the screen from dimming or locking during active usage
     * such as video playback or long-running user interactions.
     */
    fun Activity.acquireWakeLock() {
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Releases the previously acquired screen-on lock.
     *
     * Allows the system to resume normal screen timeout behavior
     * and turn off the display when appropriate.
     */
    fun Activity.releaseWakeLock() {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Secures the screen content of the Activity.
     *
     * Prevents screenshots, screen recordings, and content from appearing
     * in the recent apps overview for security or DRM-protected content.
     */
    fun Activity.secureScreen() {
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    /**
     * Removes the secure screen protection from the Activity.
     *
     * Allows screenshots, screen recordings, and normal visibility
     * in the recent apps overview again.
     */
    fun Activity.unSecureScreen() {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

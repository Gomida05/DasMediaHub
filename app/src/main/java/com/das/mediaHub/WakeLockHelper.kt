package com.das.mediaHub

import android.app.Activity
import android.view.WindowManager

internal object WakeLockHelper {
    internal fun Activity.acquireWakeLock() {
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    internal fun Activity.releaseWakeLock() {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun Activity.secureScreen() {
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun Activity.unSecureScreen() {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

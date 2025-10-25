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
}

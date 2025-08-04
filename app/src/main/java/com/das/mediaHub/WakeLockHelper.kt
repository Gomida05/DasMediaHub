package com.das.mediaHub

import android.app.Activity
import android.view.WindowManager

object WakeLockHelper {
    internal fun acquireWakeLock(activity: Activity?) {
        activity?.let {
            it.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    internal fun releaseWakeLock(activity: Activity?) {
        activity?.let {
            it.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

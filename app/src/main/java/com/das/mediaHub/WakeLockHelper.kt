package com.das.mediaHub

import android.content.Context
import android.os.PowerManager

object WakeLockHelper {
    private var wakeLock: PowerManager.WakeLock? = null

    fun acquireWakeLock(context: Context) {
        if (wakeLock?.isHeld == true) return

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "App::WakeLockTag"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes max
        }
    }

    fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }
}

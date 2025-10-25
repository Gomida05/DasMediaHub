package com.das.mediaHub

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer

internal object PIP {
    internal var shouldEnterPipMode = false

    @Composable
    internal fun MainActivity.rememberIsInPipMode(): Boolean {
        if (VERSION.SDK_INT <VERSION_CODES.O) return false

        var pipMode by remember { mutableStateOf(isInPictureInPictureMode) }
        val activity = rememberUpdatedState(this)

        DisposableEffect(Unit) {
            val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
                pipMode = info.isInPictureInPictureMode
            }
            activity.value.addOnPictureInPictureModeChangedListener(observer)
            onDispose {
                activity.value.removeOnPictureInPictureModeChangedListener(observer)
            }
        }

        return pipMode
    }

}
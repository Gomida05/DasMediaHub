package com.das.mediaHub

import android.app.Activity
import android.graphics.Rect
import android.view.View
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
    internal val shouldEnterPipMode = mutableStateOf(value =false)

    @Composable
    internal fun MainActivity.rememberIsInPipMode(): Boolean {
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

    internal fun Activity.getPipSourceRect(): Rect {
        val root = findViewById<View>(android.R.id.content)
        val rect = Rect()
        return if (root.isShown && root.getGlobalVisibleRect(rect)) {
            rect
        } else {
            Rect(0, 0, 1, 1)
        }
    }

}
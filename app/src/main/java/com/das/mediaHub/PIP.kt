package com.das.mediaHub

import android.app.Activity
import android.app.PictureInPictureParams
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer

internal object PIP {
    internal val canEnterPipMode = mutableStateOf(value = false)

    @Composable
    internal fun ComponentActivity.rememberIsInPipMode(): Boolean {
        var pipMode by remember { mutableStateOf(isInPictureInPictureMode) }
        val activity by rememberUpdatedState(this)
        val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
            pipMode = info.isInPictureInPictureMode
        }
        DisposableEffect(activity) {

            activity.addOnPictureInPictureModeChangedListener(observer)
            onDispose {
                activity.removeOnPictureInPictureModeChangedListener(observer)
            }
        }

        return pipMode
    }

    @Composable
    internal fun Modifier.rememberPipModifier(
        activity: Activity
    ): Modifier {
        return onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            val sourceRect = Rect(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            )

            val builder = PictureInPictureParams.Builder()
                .setSourceRectHint(sourceRect)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setAutoEnterEnabled(true)
            }

            // Optional: set aspect ratio based on your player UI
            val width = bounds.width.toInt().coerceAtLeast(1)
            val height = bounds.height.toInt().coerceAtLeast(1)
            builder.setAspectRatio(Rational(width, height))

            activity.setPictureInPictureParams(builder.build())
        }
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
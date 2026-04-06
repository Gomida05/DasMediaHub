package com.das.mediaHub

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer

internal object PIP {
    internal var canEnterPipMode by mutableStateOf(value = false)

    @Composable
    internal fun rememberIsInPipMode(): Boolean {
        val activity = LocalContext.current.findActivity()
        var pipMode by remember { mutableStateOf(activity?.isInPictureInPictureMode) }
        val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
            pipMode = info.isInPictureInPictureMode
        }
        DisposableEffect(activity) {

            activity?.addOnPictureInPictureModeChangedListener(observer)
            onDispose {
                activity?.removeOnPictureInPictureModeChangedListener(observer)
            }
        }

        return pipMode == true
    }

    @Composable
    internal fun Modifier.rememberPipModifier(): Modifier {

        val activity = LocalContext.current.findActivity()

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

            activity?.findActivity()?.setPictureInPictureParams(builder.build())
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

    @Composable
    fun HandlePip() {
        val shouldEnterPip by rememberUpdatedState(canEnterPipMode)
        val activity = LocalContext.current.findActivity()
        DisposableEffect(activity) {
            val onUserLeaveBehavior = Runnable {
                if (shouldEnterPip) {
                    activity?.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                }
            }
            activity?.addOnUserLeaveHintListener(
                onUserLeaveBehavior
            )
            onDispose {
                activity?.removeOnUserLeaveHintListener(
                    onUserLeaveBehavior
                )
            }
        }
    }

    internal fun Context.findActivity(): ComponentActivity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is ComponentActivity) return context
            context = context.baseContext
        }
        return null
    }
}
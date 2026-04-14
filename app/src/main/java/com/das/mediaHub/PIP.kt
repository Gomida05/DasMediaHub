package com.das.mediaHub

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.rotateScreen

internal object PIP {
    var isPlaybackActive by mutableStateOf(false)
    var allowAutoPip by mutableStateOf(false)

    val canEnterPipMode: Boolean
        get() = isPlaybackActive && allowAutoPip

    @Composable
    fun BindPip(activity: ComponentActivity?) {
        var sourceRect by remember { mutableStateOf(Rect(0, 0, 1, 1)) }
        var aspectRatio by remember { mutableStateOf(Rational(16, 9)) }
        val canEnter = canEnterPipMode

        LaunchedEffect(activity, canEnter, sourceRect, aspectRatio) {
            val builder = PictureInPictureParams.Builder()
                .setSourceRectHint(sourceRect)
                .setAspectRatio(aspectRatio)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setAutoEnterEnabled(canEnter)
            }

            activity?.setPictureInPictureParams(builder.build())
            Log.d("PIP", "setPictureInPictureParams autoEnter=$canEnter")
        }
    }

    @Composable
    fun HandlePip(activity: ComponentActivity?) {
        val shouldEnter by rememberUpdatedState(canEnterPipMode)

        DisposableEffect(activity) {
            val onUserLeaveBehavior = Runnable {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && shouldEnter) {
                    activity?.enterPictureInPictureMode(
                        PictureInPictureParams.Builder().build()
                    )
                }
            }

            activity?.addOnUserLeaveHintListener(onUserLeaveBehavior)
            onDispose {
                activity?.removeOnUserLeaveHintListener(onUserLeaveBehavior)
            }
        }
    }
    @Composable
    fun rememberIsInPipMode(): Boolean {
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
    fun Modifier.rememberPipModifier(): Modifier {
        val activity = LocalContext.current.findActivity()
        var sourceRect by remember { mutableStateOf(Rect(0, 0, 1, 1)) }
        var aspectRatio by remember { mutableStateOf(Rational(1, 1)) }
        val canEnter = canEnterPipMode

        DisposableEffect(activity, canEnter, sourceRect, aspectRatio) {
            val builder = PictureInPictureParams.Builder()
                .setSourceRectHint(sourceRect)
                .setAspectRatio(aspectRatio)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setAutoEnterEnabled(canEnter)
            }

            activity?.setPictureInPictureParams(builder.build())
            onDispose { }
        }

        return onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()

            sourceRect = Rect(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            )

            val width = bounds.width.toInt().coerceAtLeast(1)
            val height = bounds.height.toInt().coerceAtLeast(1)
            aspectRatio = Rational(width, height)
        }
    }

    fun Activity.getPipSourceRect(): Rect {
        val root = findViewById<View>(android.R.id.content)
        val rect = Rect()
        return if (root.isShown && root.getGlobalVisibleRect(rect)) {
            rect
        } else {
            Rect(0, 0, 1, 1)
        }
    }


    fun Context.findActivity(): ComponentActivity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is ComponentActivity) return context
            context = context.baseContext
        }
        return null
    }

    fun disablePipAndScreenLock(activity: ComponentActivity?) {
        allowAutoPip = false
        isPlaybackActive = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity?.setPictureInPictureParams(
                PictureInPictureParams.Builder()
                    .setAutoEnterEnabled(false)
                    .build()
            )
        }
        activity?.window?.clearFlags(FLAG_KEEP_SCREEN_ON)
        activity?.rotateScreen(fullScreen = false)
    }
}
package com.das.mediaHub.ui.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.das.mediaHub.data.model.TopPopUp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * Centralized helper for displaying a transient top popup notification
 * across the application.
 *
 * This object holds the shared state for the currently visible notification
 * and exposes a composable extension function on [TopPopUp] to render it.
 *
 * The popup appears from the top of the screen, auto-dismisses after a
 * configurable duration, and can be dismissed manually via an upward swipe.
 */
internal object TopPopupNotification {

    /**
     * Holds the currently displayed [TopPopUp] notification.
     *
     * When this value is non-null, the popup will be rendered.
     * Setting it back to null dismisses the notification.
     */
    var showNotificationDialog by mutableStateOf<TopPopUp?>(null)

    @Composable
    fun Notification() {
        showNotificationDialog?.let {
            it.PopupNotification {
                showNotificationDialog = null
            }
        }
    }

    /**
     * Displays a top popup notification for this [TopPopUp] instance.
     *
     * The notification animates in from the top, remains visible for the
     * specified duration, and then dismisses automatically.
     *
     * Users can also dismiss the popup early by dragging it upward.
     *
     * @param durationMillis Duration (in milliseconds) the popup stays visible
     * before being dismissed automatically.
     * @param onDismiss Callback invoked when the popup should be dismissed.
     */
    @Composable
    private fun TopPopUp.PopupNotification(
        durationMillis: Long = 4000,
        onDismiss: () -> Unit
    ) {
        val coroutineScope = rememberCoroutineScope()

        val heightPx = with(LocalDensity.current) { 80.dp.toPx() }
        val offsetY = remember { Animatable(-heightPx) }


        LaunchedEffect(Unit) {
            offsetY.snapTo(-heightPx)
            offsetY.animateTo(0f)

            delay(durationMillis.milliseconds)

            offsetY.animateTo(-heightPx)
            onDismiss()
        }

        val dragModifier = Modifier.pointerInput(Unit) {
            detectVerticalDragGestures(
                onVerticalDrag = { change, dragAmount ->
                    change.consume()

                    // Allow only upward dragging
                    if (dragAmount < 0f) {
                        coroutineScope.launch {
                            val newOffset = offsetY.value + dragAmount
                            offsetY.snapTo(newOffset.coerceAtMost(0f))
                        }
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        if (offsetY.value < -heightPx / 2) {
                            offsetY.animateTo(-heightPx)
                            onDismiss()
                        } else {
                            offsetY.animateTo(0f)
                        }
                    }
                }
            )
        }

        Box(
            Modifier
                .padding(6.dp)
                .fillMaxWidth()
                .zIndex(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { -100 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -100 }) + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Card(
                        elevation = CardDefaults.cardElevation(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .offset { IntOffset(0, offsetY.value.roundToInt()) }
                            .then(dragModifier)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Notification Icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (loading) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
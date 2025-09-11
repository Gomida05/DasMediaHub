package com.das.mediaHub.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.das.mediaHub.data.model.TopPopUp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal object TopPopupNotification {
    var showNotificationDialog by mutableStateOf<TopPopUp?>(null)

    @Composable
    fun TopPopupNotification(
        value: TopPopUp,
        onDismiss: () -> Unit,
        durationMillis: Long = 4000
    ) {
        val offsetY = remember { Animatable(-100f) } // start above screen
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            offsetY.snapTo(-100f)
            offsetY.animateTo(0f)
            delay(durationMillis)
            onDismiss()
        }


        val dragModifier = Modifier.pointerInput(Unit) {
            detectVerticalDragGestures(
                onVerticalDrag = { change, dragAmount ->
                    change.consume()

                    if (dragAmount < 0f) {
                        coroutineScope.launch {
                            val newOffset = offsetY.value + dragAmount
                            offsetY.snapTo(newOffset.coerceAtMost(0f)) // Prevent dragging down
                        }
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        // If dragged up far enough, dismiss
                        if (offsetY.value <= -80f) {
                            onDismiss()
                        } else {
                            // Otherwise, snap back to position
                            offsetY.animateTo(0f)
                        }
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { -100 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -100 }) + fadeOut()
        ) {
            Box(
                Modifier
                    .padding(6.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .zIndex(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    onClick = {

                    },
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .offset { IntOffset(0, offsetY.value.roundToInt()) }
                        .then(dragModifier)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = value.icon,
                            contentDescription = "Notification Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = value.message,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
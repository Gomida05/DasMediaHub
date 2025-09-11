package com.das.mediaHub.ui.players.videoPlayer

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object CustomMethods {

    fun Activity.setFullscreen(fullscreen: Boolean) {

        WindowCompat.setDecorFitsSystemWindows(window, !fullscreen)
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        if (fullscreen) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    @Composable
    fun SkeletonLoadingLayout() {
        // This can be your custom skeleton loader UI
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
                    .shimmerLoading()
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Thumbnail and other details placeholders
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(100))
                        .shimmerLoading()
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .shimmerLoading()
                        .width(142.dp)
                        .height(16.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .shimmerLoading()
                        .width(52.dp)
                        .height(16.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .shimmerLoading()
                        .width(62.dp)
                        .height(16.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons placeholders
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(22))
                            .background(Color.Gray.copy(alpha = 0.3f))
                            .shimmerLoading()
                    )
                }
            }
        }
    }

    @Composable
    fun SkeletonSuggestionLoadingLayout(
        fillMaxSize: Boolean = false
    ) {
        val modifier = if (fillMaxSize) {
            Modifier
                .fillMaxSize()
        } else{
            Modifier
                .fillMaxWidth()
                .height(495.dp)
        }

        Column(
            modifier = modifier
                .padding(8.dp)
                .shimmerLoading()
        ) {
            // Placeholder for each video item (image + text)
            repeat(5) { // Repeat for a few video items to show skeletons


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(193.dp)
                        .background(Color.Gray.copy(alpha = 0.17f))
                ) {
                    Text(
                        text = "",
                        maxLines = 1,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(end = 3.dp, bottom = 3.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                            .height(20.dp)
                            .width(50.dp)
                            .shimmerLoading()
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray.copy(alpha = 0.2f)) // Placeholder background
                ) {

                    // Channel Profile Image
                    Box(
                        modifier = Modifier
                            .size(40.dp, 40.dp)
                            .clip(RoundedCornerShape(100))
                            .shimmerLoading()
                            .background(Color.Gray.copy(alpha = 0.2f))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                    ) {
                        //Title
                        Text(
                            text = "",
                            modifier = Modifier
                                .padding(start = 6.dp, end = 6.dp)
                                .fillMaxWidth()
                                .height(16.dp)
                                .shimmerLoading()
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )

                        // Channel name, views, and date placeholders

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(12.dp)
                                    .shimmerLoading()
                                    .background(Color.Gray.copy(alpha = 0.3f))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(12.dp)
                                    .shimmerLoading()
                                    .background(Color.Gray.copy(alpha = 0.3f))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(12.dp)
                                    .shimmerLoading()
                                    .background(Color.Gray.copy(alpha = 0.3f))
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(9.dp)
                            .height(40.dp)
                            .shimmerLoading()
                            .background(Color.Gray.copy(alpha = 0.3f))

                    ) { }

                }

            }
        }
    }

    fun Modifier.shimmerLoading(
        durationMillis: Int = 1000
    ): Modifier = composed {
        val transition = rememberInfiniteTransition(label = "")

        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = ""
        )

        this.drawWithCache {
            val shimmerColors = listOf(
                Color.LightGray.copy(alpha = 0.3f),
                Color.LightGray.copy(alpha = 0.9f),
                Color.LightGray.copy(alpha = 0.3f),
            )

            val brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(translateAnim - 200f, 0f),
                end = Offset(translateAnim, size.height)
            )

            onDrawBehind {
                drawRect(brush = brush)
            }
        }
    }

}
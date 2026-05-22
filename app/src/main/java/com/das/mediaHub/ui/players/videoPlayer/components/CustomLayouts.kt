package com.das.mediaHub.ui.players.videoPlayer.components

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A collection of polished skeleton loading layouts for a premium video player experience.
 */
object CustomLayouts {

    private val PlaceholderColor = Color.Gray.copy(alpha = 0.12f)
    private val ShimmerColor = Color.White.copy(alpha = 0.25f)
    private val SecondaryPlaceholderColor = Color.Gray.copy(alpha = 0.08f)

    /**
     * Polished skeleton for video player details (Title, Subscribe, Stats, Actions).
     */
    @Composable
    fun SkeletonLoadingLayout() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Main Title Placeholder - Multi-line effect
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PlaceholderColor)
                        .shimmerLoading()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PlaceholderColor)
                        .shimmerLoading()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Channel & Subscribe Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PlaceholderColor)
                        .shimmerLoading()
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    // Channel Name
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(PlaceholderColor)
                            .shimmerLoading()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Subscriber Count
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SecondaryPlaceholderColor)
                            .shimmerLoading()
                    )
                }

                // Subscribe Button Placeholder
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PlaceholderColor)
                        .shimmerLoading()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Row (Pill shaped containers)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Like/Dislike Pill
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SecondaryPlaceholderColor)
                        .shimmerLoading()
                )
                
                // Share Pill
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SecondaryPlaceholderColor)
                        .shimmerLoading()
                )
                
                // More actions
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryPlaceholderColor)
                            .shimmerLoading()
                    )
                }
            }
        }
    }

    /**
     * Polished skeleton for suggested videos with rich detail placeholders.
     */
    @Composable
    fun SkeletonSuggestionLoadingLayout(
        fillMaxSize: Boolean = false
    ) {
        val modifier = if (fillMaxSize) Modifier.fillMaxSize() else Modifier.fillMaxWidth()

        Column(modifier = modifier.padding(vertical = 12.dp)) {
            repeat(4) {
                SuggestionItemSkeleton()
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }


    fun LazyListScope.suggestionStateCard(
        icon: ImageVector,
        title: String,
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        isError: Boolean = false
    ) {
        item {
            val containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }

            val iconTint = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = containerColor,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = iconTint.copy(alpha = 0.12f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        if (actionLabel != null && onAction != null) {
                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onAction,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(actionLabel)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SuggestionItemSkeleton() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
        ) {
            // Video Thumbnail with refined corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(horizontal = 0.dp) // Edge to edge feel
                    .background(PlaceholderColor)
                    .shimmerLoading()
            ) {
                // Timestamp tag placeholder
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(44.dp, 18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                // Channel Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PlaceholderColor)
                        .shimmerLoading()
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    // Video Title (Two lines)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(PlaceholderColor)
                            .shimmerLoading()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(PlaceholderColor)
                            .shimmerLoading()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Metadata (Channel Name • Views • Date)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SecondaryPlaceholderColor)
                            .shimmerLoading()
                    )
                }
                
                // More menu button
                Box(
                    modifier = Modifier
                        .size(16.dp, 24.dp)
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SecondaryPlaceholderColor.copy(alpha = 0.05f))
                )
            }
        }
    }

    /**
     * Advanced Shimmer Effect.
     * Features: Diagonal gradient, non-linear easing, and subtle "bloom".
     */
    private fun Modifier.shimmerLoading(
        durationMillis: Int = 1500
    ): Modifier = composed {
        val transition = rememberInfiniteTransition(label = "shimmerTransition")

        // Main movement animation
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )

        // Subtle alpha pulse for extra polish
        val alphaPulse by transition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis / 2, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shimmerPulse"
        )

        this.drawWithCache {
            val width = size.width
            val height = size.height
            
            // Wider shimmer for smoother transition
            val shimmerWidth = (width * 1.2f).coerceAtLeast(200.dp.toPx())

            val shimmerColors = listOf(
                ShimmerColor.copy(alpha = 0.0f),
                ShimmerColor.copy(alpha = 0.15f * alphaPulse),
                ShimmerColor.copy(alpha = 0.45f * alphaPulse), // The "bright" spot
                ShimmerColor.copy(alpha = 0.15f * alphaPulse),
                ShimmerColor.copy(alpha = 0.0f),
            )

            // xPosition moves from -shimmerWidth to width
            val xPosition = (width + shimmerWidth) * translateAnim - shimmerWidth

            // Diagonal brush for a more organic feel
            val brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(xPosition, 0f),
                end = Offset(xPosition + shimmerWidth, height),
                tileMode = TileMode.Clamp
            )

            onDrawBehind {
                drawRect(brush = brush)
            }
        }
    }
}

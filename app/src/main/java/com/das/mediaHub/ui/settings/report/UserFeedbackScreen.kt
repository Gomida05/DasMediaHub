package com.das.mediaHub.ui.settings.report

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.das.mediaHub.data.model.enums.FeedBackCategory
import com.das.mediaHub.data.model.enums.ModeType
import com.das.mediaHub.ui.settings.aboutBackgroundBrush

@Composable
fun UserFeedbackScreen() {
    val viewModel = hiltViewModel<UserFeedbackViewModel>()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val bg = when {
        uiState.isOverLimit -> MaterialTheme.colorScheme.errorContainer
        uiState.isNearLimit -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
    }

    val textColor = when {
        uiState.isOverLimit -> MaterialTheme.colorScheme.onErrorContainer
        uiState.isNearLimit -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearTransientMessage()
        }

        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearTransientMessage()
        }
    }


    val buttonScale by animateFloatAsState(
        targetValue = if (uiState.canSend) 1f else 0.98f,
        animationSpec = spring(),
        label = "buttonScale"
    )


    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(aboutBackgroundBrush())
        ) {
            val contentMaxWidth = if (maxWidth > 840.dp) 820.dp else 720.dp

            LazyColumn (
                contentPadding = padding,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    HeaderHero(
                        feedbackTextIsNotBlank = uiState.feedbackText.isNotBlank()
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "How are you feeling?",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MoodButton(
                                    emoji = "😍",
                                    label = "Love it",
                                    selected = uiState.selectedMood == ModeType.LoveIt,
                                    onClick = { viewModel.onMoodSelected(ModeType.LoveIt) },
                                    modifier = Modifier.weight(1f)
                                )
                                MoodButton(
                                    emoji = "😐",
                                    label = "Okay",
                                    selected = uiState.selectedMood == ModeType.Okay,
                                    onClick = { viewModel.onMoodSelected(ModeType.Okay) },
                                    modifier = Modifier.weight(1f)
                                )
                                MoodButton(
                                    emoji = "😤",
                                    label = "Broken",
                                    selected = uiState.selectedMood == ModeType.Broken,
                                    onClick = { viewModel.onMoodSelected(ModeType.Broken) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                "Category",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CategoryChip(
                                    text = "General",
                                    selected = uiState.selectedCategory == FeedBackCategory.General,
                                    icon = {
                                        Icon(
                                            Icons.Default.Feedback,
                                            null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = { viewModel.onCategorySelected(FeedBackCategory.General) }
                                )
                                CategoryChip(
                                    text = "Bug",
                                    selected = uiState.selectedCategory == FeedBackCategory.Bug,
                                    icon = {
                                        Icon(
                                            Icons.Default.BugReport,
                                            null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = { viewModel.onCategorySelected(FeedBackCategory.Bug) }
                                )
                                CategoryChip(
                                    text = "Feature",
                                    selected = uiState.selectedCategory == FeedBackCategory.Feature,
                                    icon = {
                                        Icon(
                                            Icons.Default.Lightbulb,
                                            null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = { viewModel.onCategorySelected(FeedBackCategory.Feature) }
                                )
                                CategoryChip(
                                    text = "Praise",
                                    selected = uiState.selectedCategory == FeedBackCategory.Praise,
                                    icon = {
                                        Icon(
                                            Icons.Default.Star,
                                            null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = { viewModel.onCategorySelected(FeedBackCategory.Praise) }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Quick prompts",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "The player should...",
                                    "I found a bug when...",
                                    "Please add...",
                                    "I really liked..."
                                ).forEach { prompt ->
                                    AssistChip(
                                        onClick = { viewModel.insertPrompt(prompt) },
                                        label = { Text(prompt) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.55f
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Your message",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                CounterPill(
                                    charCount = uiState.charCount,
                                    limit = uiState.charLimit,
                                    bg = bg,
                                    textColor = textColor
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = uiState.feedbackText,
                                onValueChange = { viewModel.onFeedbackTextChange(it) },
                                placeholder = {
                                    Text("Type your feedback here...")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 180.dp),
                                enabled = !uiState.isSending,
                                shape = RoundedCornerShape(22.dp),
                                maxLines = 12,
                                singleLine = false,
                                isError = uiState.isOverLimit,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.45f
                                    ),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.28f
                                    ),
                                    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.18f
                                    ),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(
                                        alpha = 0.35f
                                    ),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = { uiState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                drawStopIndicator = {},
                                gapSize = 0.dp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = uiState.statusText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = when {
                                        uiState.isOverLimit -> MaterialTheme.colorScheme.error
                                        uiState.isNearLimit -> MaterialTheme.colorScheme.tertiary
                                        uiState.feedbackText.isBlank() -> MaterialTheme.colorScheme.onSurfaceVariant
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )

                                Text(
                                    text = if (uiState.remaining >= 0) "${uiState.remaining} left" else "${-uiState.remaining} over",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (uiState.isOverLimit) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    AnimatedVisibility(
                        visible = uiState.feedbackText.isNotBlank(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = uiState.helperText,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.isOverLimit) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }



                item {
                    ElevatedButton (
                        onClick = { viewModel.sendFeedback() },
                        enabled = uiState.canSend,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .scale(buttonScale),
                        shape = RoundedCornerShape(18.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        AnimatedContent(
                            targetState = uiState.isSending,
                            label = "sendState"
                        ) { sending ->
                            if (sending) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.6.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Sending...",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Submit Feedback",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
fun HeaderHero(feedbackTextIsNotBlank: Boolean) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(700)) + slideInVertically(
            animationSpec = tween(700),
            initialOffsetY = { it / 4 }
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.80f)
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(84.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (feedbackTextIsNotBlank) {
                                Icons.Default.Feedback
                            } else {
                                Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Make this app better",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Got an idea, found something broken, or just want to hype up a feature? Drop it here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        )
    ) {
        Box(

        ) {
            content()
        }
    }
}

@Composable
private fun MoodButton(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        label = "moodBg"
    )

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(),
        label = "moodScale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .background(bg, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun CategoryChip(
    text: String,
    selected: Boolean,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = icon,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            },
            labelColor = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    )
}

@Composable
private fun CounterPill(
    charCount: Int,
    limit: Int,
    bg: Color,
    textColor: Color
) {


    val animatedCount by animateIntAsState(
        targetValue = charCount,
        label = "animatedCount"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Text(
            text = "$animatedCount / $limit",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = textColor
        )
    }
}

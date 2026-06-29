package com.das.mediaHub.ui.components.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.das.mediaHub.data.model.PlatformInfo
import com.das.mediaHub.data.model.icons.filled.InstagramIcon
import com.das.mediaHub.data.model.icons.filled.TikTokIcon
import com.das.mediaHub.data.model.icons.filled.YouTubeIcon

/**
 * A dialog that monitors the system clipboard for valid social media links.
 * When a link from a supported platform (YouTube, Instagram, TikTok) is detected,
 * it prompts the user with an [AlertDialog] to paste and process the link.
 *
 * This component uses [retain] to persist its state across navigation and
 * [RetainedEffect] to manage the clipboard listener lifecycle.
 *
 * Example usage:
 * ```kotlin
 * ClipboardAlertDialog(
 *     onPaste = { url ->
 *         viewModel.processUrl(url)
 *     }
 * )
 * ```
 *
 * @param onPaste Callback triggered when the user confirms the "Paste & Load" action.
 */
@Composable
fun ClipboardAlertDialog(onPaste: (String) -> Unit) {
    var showClipboardDialog by retain { mutableStateOf(false) }
    var clipboardLink by retain { mutableStateOf<String?>(null) }
    var lastCheckedLink by rememberSaveable { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboard.current
    val lifecycleOwner = LocalLifecycleOwner.current

    RetainedEffect(lifecycleOwner, clipboardManager) {
        val nativeClipboard = clipboardManager.nativeClipboard

        val checkClipboard = {
            if (nativeClipboard.hasPrimaryClip()) {
                val text = nativeClipboard.primaryClip?.getLastCopied()

                // Ensure text is not null before checking validity
                if (text.isValidSocialUrl() && text != lastCheckedLink) {
                    clipboardLink = text
                    lastCheckedLink = text
                    showClipboardDialog = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        nativeClipboard.clearPrimaryClip()
                    } else {
                        val newEmptyClip = ClipData.newPlainText("EmptyClipContent", "")
                        nativeClipboard.setPrimaryClip(newEmptyClip)
                    }
                }
            }
        }

        val listener = ClipboardManager.OnPrimaryClipChangedListener {
            checkClipboard()
        }
        nativeClipboard.addPrimaryClipChangedListener(listener)


        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkClipboard()
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onRetire {
            nativeClipboard.removePrimaryClipChangedListener(listener)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    if (showClipboardDialog) {
        val platformInfo = getPlatformInfo(clipboardLink)

        var animationTriggered by remember { mutableStateOf(false) }
        val iconScale by animateFloatAsState(
            targetValue = if (animationTriggered) 1f else 0.4f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "iconScaleAnim"
        )

        // Trigger the animation as soon as the dialog enters the composition
        LaunchedEffect(Unit) {
            animationTriggered = true
        }

        AlertDialog(
            onDismissRequest = {
                showClipboardDialog = false
            },
            shape = RoundedCornerShape(32.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = platformInfo.color.copy(alpha = 0.12f),
                    modifier = Modifier.scale(iconScale)
                ) {
                    Box(
                        modifier = Modifier.padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = platformInfo.icon,
                            contentDescription = "${platformInfo.name} Icon",
                            tint = platformInfo.color,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "${platformInfo.name} Link Found",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "We detected a ${platformInfo.name} link in your clipboard. Would you like to process it in DasMediaHub?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardLink?.let(onPaste)
                        showClipboardDialog = false
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text("Paste & Load", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClipboardDialog = false
                    }
                ) {
                    Text(
                        text = "Ignore",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        )
    }
}

/**
 * Determines the platform name, icon, and brand color based on the provided URL.
 *
 * Example:
 * ```kotlin
 * val info = getPlatformInfo("https://www.youtube.com/watch?v=...")
 * // Returns PlatformInfo(name="YouTube", icon=YouTubeIcon, color=Red)
 * ```
 *
 * @param url The string URL to analyze.
 * @return A [PlatformInfo] object containing the UI details for the detected platform.
 */
@Composable
fun getPlatformInfo(url: String?): PlatformInfo {
    // Replace Icons.Default.Link with your actual custom icons
    return when {
        url?.contains("youtube.com") == true || url?.contains("youtu.be") == true ->
            PlatformInfo("YouTube", Icons.Default.YouTubeIcon, Color(0xFFFF0000))

        url?.contains("instagram.com") == true ->
            PlatformInfo(
                "Instagram",
                Icons.Default.InstagramIcon /* InstagramIcon */,
                Color(0xFFE1306C)
            )

        url?.contains("tiktok.com") == true ->
            // Use MaterialTheme.colorScheme.onSurface so the TikTok icon adapts to Dark/Light mode
            PlatformInfo(
                "TikTok",
                Icons.Default.TikTokIcon /* TikTokIcon */,
                MaterialTheme.colorScheme.onSurface
            )

        else ->
            PlatformInfo("Social", Icons.Default.Link, MaterialTheme.colorScheme.primary)
    }
}

/**
 * Extension function to check if a nullable string contains a valid and supported
 * social media domain (YouTube, Instagram, or TikTok).
 *
 * Example:
 * ```kotlin
 * "https://tiktok.com/xyz".isValidSocialUrl() // returns true
 * "https://google.com".isValidSocialUrl()   // returns false
 * ```
 *
 * @return True if the string is a supported social media URL, false otherwise.
 */
fun String?.isValidSocialUrl(): Boolean {
    if (this == null) return false
    return contains("instagram.com") ||
            contains("tiktok.com") ||
            contains("youtube.com") ||
            contains("youtu.be")
}

/**
 * Extracts the plain text content from the first item in [ClipData].
 *
 * @return The text content as a [String], or null if the clip is empty.
 */
fun ClipData?.getLastCopied(): String? = this?.getItemAt(0)?.text?.toString()

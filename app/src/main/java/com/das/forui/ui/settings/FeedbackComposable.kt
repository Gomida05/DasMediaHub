package com.das.forui.ui.settings

import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.util.Locale


@Composable
fun FeedbackComposable() {

    var feedbackText by remember { mutableStateOf("") }

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val charLimit = 500
    val isOverLimit = feedbackText.length > charLimit
    var isSending by remember { mutableStateOf(false) }

    fun sendMessage() {
        if (feedbackText.isBlank()) return
        isSending = true
        sendFeedbackToFireStore(
            message = feedbackText,
            onSuccess = {
                feedbackText = ""
                isSending = false
                scope.launch {
                    snackBarHostState.showSnackbar("✅ Feedback sent. Thank you!")
                }
            },
            onFailure = {
                isSending = false
                scope.launch {
                    snackBarHostState.showSnackbar("❌ Failed to send. ${it.message}")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { safePadding ->

        Box(
            modifier = Modifier
                .padding(safePadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Send Us Your Feedback",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = {
                        if (it.length <= charLimit) feedbackText = it
                    },
                    label = { Text("Your message") },
                    placeholder = { Text("I’d like to suggest...") },
                    supportingText = {
                        val remaining = charLimit - feedbackText.length
                        Text(
                            text = "$remaining characters left",
                            color = if (isOverLimit) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = isOverLimit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp)
                        .animateContentSize(),
                    maxLines = 8,
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                SendFeedbackButton(
                    enabled = feedbackText.isNotBlank() && !isSending && !isOverLimit,
                    isLoading = isSending
                ) {
                    sendMessage()
                }
            }
        }
    }
}

@Composable
private fun SendFeedbackButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "scale"
    )

    ElevatedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 20.dp),
        interactionSource = interactionSource
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = "Send",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}



private fun sendFeedbackToFireStore(
    message: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = Firebase.firestore

    // Get user device info
    val deviceModel = Build.MODEL ?: "Unknown"
    val androidVersion = Build.VERSION.RELEASE ?: "Unknown"
    val deviceManufacturer = Build.MANUFACTURER ?: "Unknown"
    val region = Locale.getDefault().toString() // e.g. "en_US"

    val feedbackData = hashMapOf(
        "message" to message,
        "timestamp" to System.currentTimeMillis(),
        "deviceModel" to deviceModel,
        "deviceManufacturer" to deviceManufacturer,
        "androidVersion" to androidVersion,
        "region" to region
    )

    db.collection("user_feedback")
        .add(feedbackData)
        .addOnSuccessListener {
            Log.d("Feedback", "Submitted successfully")
            onSuccess()
        }
        .addOnFailureListener {
            onFailure(it)
            Log.e("Feedback", "Submission failed: ${it.message}")
        }

}
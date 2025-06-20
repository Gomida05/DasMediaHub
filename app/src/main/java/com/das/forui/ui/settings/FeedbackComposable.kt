package com.das.forui.ui.settings

import android.os.Build
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale


@Composable
fun FeedbackComposable() {

    var feedbackText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Send us your Feedback",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                label = { Text("Enter your message") },
                placeholder = { Text("I want to report...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(24.dp))

            SendFeedbackButton (
                text = "Send",
                isEmpty = {
                    feedbackText.isNotBlank()
                },
                Icons.AutoMirrored.Default.Send,
                onClick = {
                    if (feedbackText.isNotBlank()) {
                        sendFeedbackToFireStore(feedbackText)
                        feedbackText = ""
                    }
                }
            )
        }
    }
}

@Composable
private fun SendFeedbackButton(
    text: String,
    isEmpty: ()-> Boolean,
    icon: ImageVector,

    onClick: () -> Unit
){
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate icon scale on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "iconScale"
    )

    ElevatedButton(
        onClick = onClick,
        enabled = isEmpty(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 20.dp),
        interactionSource = interactionSource
    ) {

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier
                .size(25.dp)
                .padding(end = 8.dp)
                .scale(scale)
        )
    }
}



fun sendFeedbackToFireStore(message: String) {
    val db = FirebaseFirestore.getInstance()

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
        }
        .addOnFailureListener {
            Log.e("Feedback", "Submission failed: ${it.message}")
        }
}
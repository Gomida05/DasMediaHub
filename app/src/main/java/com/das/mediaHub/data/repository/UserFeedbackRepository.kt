package com.das.mediaHub.data.repository

import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Locale

class UserFeedbackRepository {

    suspend fun sendFeedback(message: String): DocumentReference {
        val db = Firebase.firestore

        val feedbackData = hashMapOf(
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "deviceModel" to (Build.MODEL ?: "Unknown"),
            "deviceManufacturer" to (Build.MANUFACTURER ?: "Unknown"),
            "androidVersion" to (Build.VERSION.RELEASE ?: "Unknown"),
            "region" to Locale.getDefault().toString()
        )

        return db.collection("user_feedback")
            .add(feedbackData)
            .await()
    }
}
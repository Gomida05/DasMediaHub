package com.das.mediaHub.data.repository

import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository responsible for sending user feedback to Firebase Firestore.
 *
 * It collects the feedback message along with basic device metadata (model, OS version, 
 * locale) to help developers diagnose issues.
 *
 * Example usage:
 * ```kotlin
 * @Inject lateinit var repository: UserFeedbackRepository
 * repository.sendFeedback("The search is a bit slow on my device.")
 * ```
 */
@Singleton
class UserFeedbackRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {


    /**
     * Uploads user feedback to the "user_feedback" Firestore collection.
     * 
     * @param message The feedback message written by the user.
     * @throws Exception if the upload fails.
     */
    suspend fun sendFeedback(message: String) {
        val feedbackData = hashMapOf(
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "deviceModel" to (Build.MODEL ?: "Unknown"),
            "deviceManufacturer" to (Build.MANUFACTURER ?: "Unknown"),
            "androidVersion" to (Build.VERSION.RELEASE ?: "Unknown"),
            "region" to Locale.getDefault().toString()
        )

        firestore.collection("user_feedback")
            .add(feedbackData)
            .await()
    }
}

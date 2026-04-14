package com.das.mediaHub.data.model.state

import com.das.mediaHub.data.model.FeedBackCategory
import com.das.mediaHub.data.model.ModeType

data class UserFeedbackUiState(
    val feedbackText: String = "",
    val selectedCategory: FeedBackCategory = FeedBackCategory.General,
    val selectedMood: ModeType? = null,
    val isSending: Boolean = false,
    val message: String? = null,
    val error: String? = null
) {
    val charLimit: Int = 500
    val charCount: Int get() = feedbackText.length
    val progress: Float get() = (charCount.toFloat() / charLimit).coerceIn(0f, 1f)
    val remaining: Int get() = charLimit - charCount
    val isNearLimit: Boolean get() = charCount >= 400
    val isOverLimit: Boolean get() = charCount > charLimit
    val canSend: Boolean get() = feedbackText.isNotBlank() && !isSending && !isOverLimit
    val helperText: String
        get() = when {
            isOverLimit -> "Trim your message a bit before sending."
            isNearLimit -> "You’re close to the limit. Keep only the most useful details."
            else -> "Helpful feedback usually says what happened, what you expected, and where it happened."
        }
    val statusText: String
        get() = when {
            isOverLimit -> "Too long"
            isNearLimit -> "Almost full"
            feedbackText.isBlank() -> "Start typing"
            else -> "Looking good"
        }
}
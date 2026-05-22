package com.das.mediaHub.data.model.state

import com.das.mediaHub.data.model.FeedBackCategory
import com.das.mediaHub.data.model.ModeType

/**
 * Data class representing the state of the User Feedback screen.
 *
 * It manages the feedback text, category selection, mood rating, and the 
 * status of the sending process.
 *
 * @property feedbackText The current text input from the user.
 * @property selectedCategory The selected [FeedBackCategory].
 * @property selectedMood The selected [ModeType] (rating).
 * @property isSending Whether the feedback is currently being uploaded.
 * @property message Success message after sending.
 * @property error Error message if sending fails.
 */
data class UserFeedbackUiState(
    val feedbackText: String = "",
    val selectedCategory: FeedBackCategory = FeedBackCategory.General,
    val selectedMood: ModeType? = null,
    val isSending: Boolean = false,
    val message: String? = null,
    val error: String? = null
) {
    /** Maximum number of characters allowed in the feedback. */
    val charLimit: Int = 500
    
    /** Current number of characters in the feedback text. */
    val charCount: Int
        get() = feedbackText.length

    /** Progress ratio toward the character limit (0.0 to 1.0). */
    val progress: Float
        get() = (charCount.toFloat() / charLimit).coerceIn(0f, 1f)

    /** Number of characters remaining before the limit. */
    val remaining: Int
        get() = charLimit - charCount

    /** Whether the character count is approaching the limit. */
    val isNearLimit: Boolean
        get() = charCount >= 400

    /** Whether the character count has exceeded the limit. */
    val isOverLimit: Boolean
        get() = charCount > charLimit

    /** Whether the feedback is valid and ready to be sent. */
    val canSend: Boolean
        get() = feedbackText.isNotBlank() && !isSending && !isOverLimit

    /** Helper text providing guidance based on current character count. */
    val helperText: String
        get() = when {
            isOverLimit -> "Trim your message a bit before sending."
            isNearLimit -> "You’re close to the limit. Keep only the most useful details."
            else -> "Helpful feedback usually says what happened, what you expected, and where it happened."
        }
        
    /** Status text describing the current state of the input. */
    val statusText: String
        get() = when {
            isOverLimit -> "Too long"
            isNearLimit -> "Almost full"
            feedbackText.isBlank() -> "Start typing"
            else -> "Looking good"
        }
}

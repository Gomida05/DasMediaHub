package com.das.mediaHub.ui.settings.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.model.FeedBackCategory
import com.das.mediaHub.data.model.ModeType
import com.das.mediaHub.data.model.state.UserFeedbackUiState
import com.das.mediaHub.data.repository.UserFeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserFeedbackViewModel @Inject constructor(
    private val repository: UserFeedbackRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(UserFeedbackUiState())
    val uiState = _uiState.asStateFlow()

    fun onFeedbackTextChange(value: String) {
        _uiState.update {
            it.copy(
                feedbackText = value.take(540),
                message = null,
                error = null
            )
        }
    }

    fun onCategorySelected(category: FeedBackCategory) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                message = null,
                error = null
            )
        }
    }

    fun onMoodSelected(mood: ModeType) {
        _uiState.update {
            it.copy(
                selectedMood = if (it.selectedMood == mood) null else mood,
                message = null,
                error = null
            )
        }
    }



    fun insertPrompt(prompt: String) {
        _uiState.update { state ->
            val newValue = when {
                state.feedbackText.isBlank() -> prompt
                state.feedbackText.contains(prompt, ignoreCase = true) -> state.feedbackText
                else -> "${state.feedbackText}\n\n$prompt"
            }

            state.copy(
                feedbackText = newValue.take(540),
                message = null,
                error = null
            )
        }
    }

    fun clearTransientMessage() {
        _uiState.update {
            it.copy(message = null, error = null)
        }
    }

    fun sendFeedback() {
        val current = _uiState.value
        if (!current.canSend) return

        val finalMessage = buildString {
            append("Category: ${current.selectedCategory.label}\n")
            if (!current.selectedMood?.label.isNullOrBlank()) {
                append("Mood: ${current.selectedMood.label}\n")
            }
            append("\n")
            append(current.feedbackText.trim())
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, message = null, error = null) }

            runCatching {
                repository.sendFeedback(finalMessage)
            }.onSuccess {
                _uiState.update {
                    UserFeedbackUiState(
                        message = "Sent successfully ✨ Thanks for helping improve the app."
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        error = "Couldn’t send feedback: ${throwable.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }
}
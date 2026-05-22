package com.das.mediaHub.data.model.state

/**
 * Sealed interface representing the state of a UI component or data fetch operation.
 *
 * This is used to handle different UI states (Loading, Success, Error, etc.) 
 * in a type-safe manner.
 *
 * @param T The type of data associated with the [Success] state.
 *
 * Example usage:
 * ```kotlin
 * when (uiState) {
 *     is UiState.Loading -> // Show spinner
 *     is UiState.Success -> // Show data using uiState.data
 *     is UiState.Error -> // Show error message uiState.message
 *     // ...
 * }
 * ```
 */
sealed interface UiState<out T> {
    /** The initial state before any action is performed. */
    object Idle : UiState<Nothing>
    
    /** Indicates that a data fetch or background operation is in progress. */
    object Loading : UiState<Nothing>
    
    /** Indicates that the operation was successful but returned no data. */
    object Empty : UiState<Nothing>
    
    /** 
     * Indicates that the operation failed.
     * @property message Descriptive error message.
     */
    data class Error(val message: String) : UiState<Nothing>
    
    /** 
     * Indicates that the operation completed successfully with data.
     * @property data The resulting data of type [T].
     */
    data class Success<T>(val data: T) : UiState<T>
}

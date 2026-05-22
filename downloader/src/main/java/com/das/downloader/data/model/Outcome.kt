package com.das.downloader.data.model

/**
 * Represents the possible outcomes of a download attempt.
 */
sealed class Outcome {
    /** Task finished successfully. */
    data object Completed : Outcome()

    /** Task was paused by user request. */
    data object Paused : Outcome()

    /** Task was canceled and resources cleaned up. */
    data object Canceled : Outcome()

    /** Task failed due to an error. */
    data class Failed(val message: String) : Outcome()
}
package com.das.mediaHub.data.error

import com.chaquo.python.PyException
import com.das.python.exceptions.PyCallError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility object for mapping exceptions and raw error strings into user-friendly, 
 * localized error messages.
 *
 * This centralizes error handling across the app, ensuring that technical logs 
 * (like Python tracebacks or HTTP 403 errors) are translated into messages 
 * that users can understand and act upon.
 */
object ErrorMapper {

    // Centralized user-facing messages for consistency and easy updates
    private const val MSG_NO_INTERNET = "No internet connection. Please check your network and try again."
    private const val MSG_TIMEOUT = "The request took too long. Please try again."
    private const val MSG_NETWORK_ERROR = "A network error occurred. Please try again."
    private const val MSG_FORBIDDEN = "Access was denied. Please try again later."
    private const val MSG_NOT_FOUND = "We couldn’t find what you were looking for."
    private const val MSG_TOO_MANY_REQUESTS = "Too many requests. Please wait a moment and try again."
    private const val MSG_SERVER_ERROR = "The server is having trouble right now. Please try again later."
    private const val MSG_DATA_ERROR = "We received an unexpected response or data error. Please try again."
    private const val MSG_PROCESSING_ERROR = "Something went wrong while processing your request."
    const val MSG_GENERIC = "Something went wrong. Please try again."

    /**
     * Maps a [Throwable] to a user-friendly error string.
     * 
     * @param throwable The exception to map.
     * @return A localized error message.
     */
    fun map(throwable: Throwable?): String {
        return when (throwable) {
            is UnknownHostException -> MSG_NO_INTERNET
            is SocketTimeoutException -> MSG_TIMEOUT
            is IOException -> MSG_NETWORK_ERROR
            is PyCallError -> mapPythonError(throwable) // Added handling for new sealed class
            is PyException -> mapPythonError(throwable)
            else -> MSG_GENERIC
        }
    }

    /**
     * Analyzes a raw error message string and returns a user-friendly version.
     *
     * @param message The raw error message (e.g., from a server response).
     * @return A user-friendly error string.
     */
    fun mapMessage(message: String?): String {
        if (message.isNullOrBlank()) {
            return MSG_GENERIC
        }
        return analyzeErrorMessage(message.lowercase())
    }

    /**
     * Specifically maps [PyException]s, which often contain complex tracebacks.
     */
    private fun mapPythonError(error: PyException): String {
        val message = error.message
        if (message.isNullOrBlank()) {
            return MSG_PROCESSING_ERROR
        }

        val matchedMessage = analyzeErrorMessage(message.lowercase())

        // If the shared analyzer couldn't find a specific match, default to a processing error
        // rather than the standard generic error, to hint that it was a Python-layer issue.
        return if (matchedMessage == MSG_GENERIC) MSG_PROCESSING_ERROR else matchedMessage
    }

    /**
     * Specifically maps [PyCallError]s, handling internal wrapper exceptions cleanly.
     */
    private fun mapPythonError(error: PyCallError): String {
        return when (error) {
            is PyCallError.NotStarted,
            is PyCallError.ModuleNotFound,
            is PyCallError.FunctionNotFound -> {
                // These represent internal app configuration/integration issues.
                MSG_PROCESSING_ERROR
            }

            is PyCallError.InvalidJson -> {
                // Maps directly to data parsing errors.
                MSG_DATA_ERROR
            }

            is PyCallError.PythonException -> {
                // Delegate to mapping the underlying cause (e.g., a PyException or network issue inside Python)
                val cause = error.cause

                if (cause != null) {
                    val mappedCause = map(cause)
                    if (mappedCause != MSG_GENERIC) {
                        return mappedCause
                    }
                }

                // If no cause or it maps to generic, fallback to analyzing the message string
                val message = error.message
                if (message.isNullOrBlank()) {
                    MSG_PROCESSING_ERROR
                } else {
                    val matchedMessage = analyzeErrorMessage(message.lowercase())
                    if (matchedMessage == MSG_GENERIC) MSG_PROCESSING_ERROR else matchedMessage
                }
            }
        }
    }

    /**
     * Shared logic to map string-based error messages (both HTTP and Python tracebacks)
     * to user-friendly UI strings.
     */
    private fun analyzeErrorMessage(msg: String): String {
        return when {
            msg.containsAny(
                "unknownhostexception",
                "failed to establish a new connection",
                "temporary failure in name resolution",
                "name or service not known",
                "[errno 7]",
                "no address associated with hostname"
            ) -> MSG_NO_INTERNET

            msg.containsAny(
                "sockettimeoutexception",
                "timed out",
                "timeout"
            ) -> MSG_TIMEOUT

            msg.containsAny("http 403", "403 forbidden", "403") -> MSG_FORBIDDEN

            msg.containsAny("http 404", "not found", "404") -> MSG_NOT_FOUND

            msg.containsAny("http 429", "too many requests", "429") -> MSG_TOO_MANY_REQUESTS

            msg.containsAny(
                "http 500", "http 502", "http 503",
                "server error", "500", "502", "503"
            ) -> MSG_SERVER_ERROR

            msg.containsAny(
                "json", "decode", "parse", "jsondecodeerror",
                "valueerror", "keyerror", "typeerror"
            ) -> MSG_DATA_ERROR

            msg.containsAny("python", "pyexception", "traceback") -> MSG_PROCESSING_ERROR

            else -> MSG_GENERIC
        }
    }

    /**
     * Helper extension function to cleanly check if a string contains ANY of the provided keywords.
     */
    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { keyword -> this.contains(keyword) }
    }
}
package com.das.mediaHub.data.error

import com.chaquo.python.PyException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMapper {

    fun map(throwable: Throwable?): String {
        return when (throwable) {
            is UnknownHostException ->
                "No internet connection. Please check your network and try again."

            is SocketTimeoutException ->
                "The request took too long. Please try again."

            is IOException ->
                "A network error occurred. Please try again."

            is PyException ->
                mapPythonError(throwable)

            else ->
                "Something went wrong. Please try again."
        }
    }

    fun mapMessage(message: String?): String {
        if (message.isNullOrBlank()) {
            return "Something went wrong. Please try again."
        }

        val msg = message.lowercase()

        return when {
            "unknownhostexception" in msg ||
                    "failed to establish a new connection" in msg ||
                    "temporary failure in name resolution" in msg ||
                    "name or service not known" in msg ->
                "No internet connection. Please check your network."

            "sockettimeoutexception" in msg ||
                    "timed out" in msg ||
                    "timeout" in msg ->
                "The request took too long. Please try again."

            "http 403" in msg || "403 forbidden" in msg ->
                "Access was denied. Please try again later."

            "http 404" in msg || "not found" in msg ->
                "We couldn’t find what you were looking for."

            "http 429" in msg || "too many requests" in msg ->
                "Too many requests. Please wait a moment and try again."

            "http 500" in msg || "http 502" in msg || "http 503" in msg || "server error" in msg ->
                "The server is having trouble right now. Please try again later."

            "json" in msg || "decode" in msg || "parse" in msg ->
                "We received an unexpected response. Please try again."

            "python" in msg || "pyexception" in msg || "traceback" in msg ->
                "Something went wrong while processing your request."

            else ->
                "Something went wrong. Please try again."
        }
    }

    private fun mapPythonError(error: PyException): String {
        val message = error.message.orEmpty().lowercase()

        return when {
            "unknownhostexception" in message ||
                    "failed to establish a new connection" in message ||
                    "temporary failure in name resolution" in message ->
                "No internet connection. Please check your network."

            "timed out" in message || "timeout" in message ->
                "The request took too long. Please try again."

            "403" in message ->
                "Access was denied. Please try again later."

            "404" in message ->
                "We couldn’t find what you were looking for."

            "429" in message || "too many requests" in message ->
                "Too many requests. Please wait a moment and try again."

            "500" in message || "502" in message || "503" in message ->
                "The server is having trouble right now. Please try again later."

            "jsondecodeerror" in message ||
                    "valueerror" in message ||
                    "keyerror" in message ||
                    "typeerror" in message ->
                "Something went wrong while processing the data."

            else ->
                "Something went wrong while processing your request."
        }
    }
}
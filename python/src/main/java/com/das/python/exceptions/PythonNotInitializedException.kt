package com.das.python.exceptions

import com.das.python.PythonMain

/**
 * Exception thrown when the Python runtime is accessed
 * before it has been started via [PythonMain.startPython].
 *
 * Example of how to avoid this:
 * ```
 * // In your Application class
 * override fun onCreate() {
 *     super.onCreate()
 *     PythonMain.apply { startPython() }
 * }
 * ```
 */
class PythonNotInitializedException(
    message: String = "Python runtime has not been started. " +
            "Call startPython() in Application.onCreate() before accessing Python functions."
) : IllegalStateException(message)

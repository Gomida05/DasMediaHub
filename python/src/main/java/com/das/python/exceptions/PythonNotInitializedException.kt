package com.das.python.exceptions

import com.das.python.PythonMain

/**
 * Exception thrown when Python runtime is accessed
 * before it has been started via [PythonMain.startPython].
 */
class PythonNotInitializedException(
    message: String = "Python runtime has not been started. " +
            "Call startPython() in Application.onCreate() before accessing Python functions."
) : IllegalStateException(message)
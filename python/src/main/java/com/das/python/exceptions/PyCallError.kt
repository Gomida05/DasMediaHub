package com.das.python.exceptions

sealed class PyCallError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NotStarted : PyCallError("Python is not started")
    class ModuleNotFound(val module: String) : PyCallError("Python module not found: $module")
    class FunctionNotFound(val module: String, val function: String) :
        PyCallError("Python function not found: $module.$function")
    class PythonException(cause: Throwable) : PyCallError("Python threw an exception", cause)
    class InvalidJson(val raw: String, cause: Throwable) : PyCallError("Invalid JSON from Python: $raw", cause)
}
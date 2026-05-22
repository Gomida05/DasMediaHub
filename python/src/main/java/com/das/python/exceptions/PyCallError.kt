package com.das.python.exceptions

/**
 * Sealed class representing various errors that can occur during
 * communication with the embedded Python runtime.
 */
sealed class PyCallError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * Thrown when an operation is attempted before Python is started.
     */
    class NotStarted : PyCallError("Python is not started")

    /**
     * Thrown when a specified Python module cannot be found.
     * @property module The name of the missing module.
     */
    class ModuleNotFound(val module: String) : PyCallError("Python module not found: $module")

    /**
     * Thrown when a specified function is missing in a Python module.
     * @property module The name of the module.
     * @property function The name of the missing function.
     */
    class FunctionNotFound(val module: String, val function: String) :
        PyCallError("Python function not found: $module.$function")

    /**
     * Thrown when an exception is raised within the Python environment.
     * @property cause The underlying Python exception.
     */
    class PythonException(cause: Throwable) : PyCallError("Python threw an exception", cause)

    /**
     * Thrown when the JSON returned by Python cannot be deserialized.
     * @property raw The raw JSON string that failed to parse.
     * @property cause The serialization exception.
     */
    class InvalidJson(val raw: String, cause: Throwable) : PyCallError("Invalid JSON from Python: $raw", cause)
}

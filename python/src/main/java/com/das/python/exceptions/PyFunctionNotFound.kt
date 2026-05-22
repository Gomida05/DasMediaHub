package com.das.python.exceptions

/**
 * Exception thrown when a requested Python function is not found
 * within the specified module.
 *
 * @param module The module where the function was expected.
 * @param function The name of the missing function.
 */
class PyFunctionNotFound(module: String, function: String) :
    NoSuchMethodException("Python function not found: $module.$function")

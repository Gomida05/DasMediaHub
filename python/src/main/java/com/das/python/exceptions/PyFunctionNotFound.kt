package com.das.python.exceptions

class PyFunctionNotFound(module: String, function: String) :
    NoSuchMethodException("Python function not found: $module.$function")
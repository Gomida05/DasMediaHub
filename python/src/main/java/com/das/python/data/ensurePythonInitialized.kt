package com.das.python.data

import com.das.python.exceptions.PythonNotInitializedException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Helper inline function to enforce initialization with contracts.
 * Use inside property getters or functions that need Python.
 */
@OptIn(ExperimentalContracts::class)
fun ensurePythonInitialized(isInitialized: Boolean) {
    contract {
        returns() implies isInitialized
    }
    if (!isInitialized) {
        throw PythonNotInitializedException()
    }
}
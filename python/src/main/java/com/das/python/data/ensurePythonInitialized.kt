package com.das.python.data

import com.das.python.exceptions.PythonNotInitializedException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Helper inline function to enforce Python runtime initialization using contracts.
 *
 * Use this inside property getters or functions that depend on the Python runtime.
 *
 * Example:
 * ```
 * fun myPythonFunction() {
 *     ensurePythonInitialized(Python.isStarted())
 *     // safe to use Python here
 * }
 * ```
 *
 * @param isInitialized Boolean indicating if Python is started.
 * @throws PythonNotInitializedException if [isInitialized] is false.
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

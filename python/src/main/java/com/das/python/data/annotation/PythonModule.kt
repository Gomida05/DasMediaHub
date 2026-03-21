package com.das.python.data.annotation

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.*

/**
 * Indicates that the annotated parameter or property
 * must be a valid Python module name.
 *
 * Can be used for parameters like `module: String`
 * in functions that call Python code.
 *
 * Example:
 * ```
 * suspend inline fun <reified T> Python.callMethod(
 *     name: Names,
 *     args: String,
 *     @PythonModule module: String = "main"
 * ) { ... }
 * ```
 */
@MustBeDocumented
@Retention(BINARY)
@Target(FUNCTION, VALUE_PARAMETER, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class PythonModule

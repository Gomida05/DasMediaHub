package com.das.python.data.annotation

/**
 * Annotation used to map a Kotlin function to a Python function.
 *
 * @property name The name of the function in the Python module.
 * If empty, the Kotlin function name is assumed.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class PyFunction(val name: String = "")

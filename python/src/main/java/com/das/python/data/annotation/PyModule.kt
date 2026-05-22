package com.das.python.data.annotation

/**
 * Annotation used to specify the Python module associated with
 * an interface or class.
 *
 * @property name The name of the Python file (e.g., "main").
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class PyModule(val name: String)

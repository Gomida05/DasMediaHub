package com.das.python.data.annotation

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.*

/**
 * Marks that a function or property requires Python runtime initialization.
 *
 * With contracts, the compiler can check calls and warn
 * if the runtime might not be initialized.
 */
@MustBeDocumented
@Retention(BINARY)
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class RequiresPythonInit

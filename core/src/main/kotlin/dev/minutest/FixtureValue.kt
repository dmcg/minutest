package dev.minutest


/**
 * Fixtures are initialised through a series of operations, which might fail. This wraps the last successful value
 * and the exception that represents any failure.
 */
data class FixtureValue<F>(val value: F, val error: Throwable? = null)

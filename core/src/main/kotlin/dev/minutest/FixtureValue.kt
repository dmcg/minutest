package dev.minutest


/**
 * Fixtures are initialised through a series of operations, which might fail. This wraps the last successful value
 * and the exception that represents any failure.
 */
data class FixtureValue<F>(val value: F, val error: Throwable? = null)

inline fun <F> FixtureValue<F>.map(f: (F) -> F): FixtureValue<F> =
    if (error != null) this else FixtureValue(f(this.value))

inline fun <F> FixtureValue<F>.flatMap(f: (F) -> FixtureValue<F>): FixtureValue<F> =
    if (error != null) this else f(this.value)


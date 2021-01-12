package dev.minutest.experimental

import dev.minutest.Annotatable

/**
 * An experimental feature that allows contexts and tests to be annotated in order to change their execution.
 */
interface TestAnnotation {
    fun applyTo(annotatable: Annotatable<*>)
}
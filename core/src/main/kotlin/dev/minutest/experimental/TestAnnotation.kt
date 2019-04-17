package dev.minutest.experimental

import dev.minutest.NodeBuilder

/**
 * An experimental feature that allows contexts and tests to be annotated in order to change their execution.
 */
interface TestAnnotation {
    fun applyTo(nodeBuilder: NodeBuilder<*>)
}
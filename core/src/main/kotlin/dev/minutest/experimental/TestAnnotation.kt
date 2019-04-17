package dev.minutest.experimental

import dev.minutest.NodeBuilder

/**
 * An experimental feature that allows contexts and tests to be annotated in order to change their execution.
 */
interface TestAnnotation {
    fun applyTo(nodeBuilder: NodeBuilder<*>)
}

/**
 * A [TestAnnotation] that just adds itself to the node as marker to be found by later processing.
 */
class MarkerAnnotation : TestAnnotation {
    override fun applyTo(nodeBuilder: NodeBuilder<*>) {
        nodeBuilder.addMarker(this)
    }
}
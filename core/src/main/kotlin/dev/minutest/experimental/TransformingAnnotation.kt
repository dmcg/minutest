package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeBuilder
import dev.minutest.NodeTransform

/**
 * Convenience implementation of [TestAnnotation].
 */
class TransformingAnnotation(
    private val transform: NodeTransform<*>
) : TestAnnotation {

    override fun applyTo(nodeBuilder: NodeBuilder<*>) {
        nodeBuilder.addTransform(transform as (Node<out Any?>) -> Nothing)
    }
}
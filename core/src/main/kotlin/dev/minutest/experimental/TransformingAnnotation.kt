package dev.minutest.experimental

import dev.minutest.NodeBuilder
import dev.minutest.NodeTransform

/**
 * Convenience implementation of [TestAnnotation].
 */
class TransformingAnnotation(
    private val transform: NodeTransform<*>
) : TestAnnotation {

    override fun applyTo(nodeBuilder: NodeBuilder<*>) {
        val transforms = nodeBuilder.transforms as MutableList<NodeTransform<*>>
        transforms.add(0, transform)
    }
}
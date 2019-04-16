package dev.minutest.experimental

import dev.minutest.NodeBuilder
import dev.minutest.NodeTransform

/**
 * Convenience implementation of [TestAnnotation].
 */
open class TransformingAnnotation<F>(
    private val transform: NodeTransform<F>
) : TestAnnotation<F> {

    override fun applyTo(nodeBuilder: NodeBuilder<F>) {
        nodeBuilder.transforms.add(0,  transform)
    }
}
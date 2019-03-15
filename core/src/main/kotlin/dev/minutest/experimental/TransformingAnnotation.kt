package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeTransform

/**
 * Convenience implementation of [TestAnnotation].
 */
open class TransformingAnnotation<in F>(
    private val transform: NodeTransform<F>
) : TestAnnotation<F> {

    constructor(transform: (Node<F>) -> Node<F>) : this(NodeTransform.create(transform))

    @Suppress("UNCHECKED_CAST")
    override fun <F2 : F> transformOfType() = transform as NodeTransform<F2>
}
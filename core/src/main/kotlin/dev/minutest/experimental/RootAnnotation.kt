package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeTransform

/**
 * Convenience implementation of [TestAnnotation].
 */
open class RootAnnotation<in T>(
    private val transform: NodeTransform<Unit>
) : TestAnnotation<T> {

    constructor(transform: (Node<Unit>) -> Node<Unit>) : this(NodeTransform.create(transform))

    override val rootTransform = transform
}
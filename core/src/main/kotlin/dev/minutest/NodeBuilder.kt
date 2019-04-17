package dev.minutest

import dev.minutest.experimental.TestAnnotation

/**
 * Common interface for the DSL components that build [Node]s.
 */
interface NodeBuilder<F> {

    fun buildNode(): Node<F>

    /**
     * Experimental - see [TestAnnotation].
     */
    fun addAnnotation(annotation: Any)

    fun addTransform(transform: NodeTransform<F>)
}

/**
 * Marker interface for the root [Context] builder.
 */
interface RootContextBuilder : NodeBuilder<Unit> {
    fun withName(newName: String): RootContextBuilder
}
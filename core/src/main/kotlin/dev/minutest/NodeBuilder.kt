package dev.minutest

import dev.minutest.experimental.TestAnnotation

/**
 * Common interface for the DSL components that build [Node]s.
 */
interface NodeBuilder<F> : Annotatable<F>{

    fun buildNode(): Node<F>
}

interface Annotatable<F> {

    /**
     * Experimental - see [TestAnnotation].
     */
    fun addMarker(marker: Any)

    /**
     * Experimental - see [TestAnnotation].
     */
    fun addTransform(transform: NodeTransform<F>)
}

/**
 * Marker interface for the root [Context] block.
 */
interface RootContextBuilder : NodeBuilder<Unit> {
    fun withNameUnlessSpecified(newName: String): RootContextBuilder
}
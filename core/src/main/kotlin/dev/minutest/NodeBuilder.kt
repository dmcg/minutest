package dev.minutest

import dev.minutest.experimental.TestAnnotation

interface NodeBuilder<F> {
    fun buildNode(): Node<F>

    /**
     * Experimental - see [TestAnnotation].
     */
    val annotations: MutableList<TestAnnotation>
}

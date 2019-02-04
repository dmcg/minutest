package uk.org.minutest

import uk.org.minutest.experimental.TestAnnotation

interface NodeBuilder<F> {
    val annotations: MutableList<TestAnnotation>
    fun buildNode(): Node<F>
}

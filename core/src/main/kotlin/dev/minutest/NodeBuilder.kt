package dev.minutest

import dev.minutest.experimental.TestAnnotation

interface NodeBuilder<F> {
    val annotations: MutableList<TestAnnotation>
    fun buildNode(): Node<F>
}

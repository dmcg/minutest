package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeBuilder

/**
 * For use by annotation transforms to establish if they should.
 */
fun TestAnnotation.appliesTo(node: Node<*>) = node.annotations.contains(this)

/**
 * Add a list of annotations to a NodeBuilder.
 */
fun <F> NodeBuilder<F>.prependAnnotations(annotations: Iterable<TestAnnotation>) {
    annotations.reversed().forEach {
        it.applyTo(this)
    }
}

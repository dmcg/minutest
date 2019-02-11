package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeBuilder
import dev.minutest.TestContextBuilder

/**
 * An experimental feature that allows contexts and tests to be annotated in order to change their execution.
 */
interface TestAnnotation {

    /**
     * Used (ironically) to *add* an annotation to a context or test block.
     */
    operator fun <F, NodeBuilderT: NodeBuilder<F>> minus(nodeBuilder: NodeBuilderT): NodeBuilderT =
        nodeBuilder.also {
            this.applyTo(it)
        }

    /**
     * Groups annotations into a list.
     */
    operator fun plus(that: TestAnnotation) = listOf(this, that)

}

/**
 * Add a list of annotations to a context or test block.
 */
operator fun <F, NodeBuilderT: NodeBuilder<F>> Iterable<TestAnnotation>.minus(nodeBuilder: NodeBuilderT): NodeBuilderT=
    nodeBuilder.also {
        this.forEach { annotation ->
            annotation.applyTo(nodeBuilder)
        }
    }

/**
 * Adds an annotation to a context block from the inside.
 */
fun TestContextBuilder<*, *>.annotateWith(annotation: TestAnnotation) {
    annotation.applyTo(this as NodeBuilder<*>)
}

/**
 * For use by annotation transforms to establish if they should.
 */
fun TestAnnotation.appliesTo(node: Node<*>) = node.annotations.contains(this)


private fun TestAnnotation.applyTo(nodeBuilder: NodeBuilder<*>) {
    nodeBuilder.annotations.add(this)
}

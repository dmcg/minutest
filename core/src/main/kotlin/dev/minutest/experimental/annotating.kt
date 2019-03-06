package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeBuilder
import dev.minutest.NodeTransform
import dev.minutest.TestContextBuilder

/**
 * An experimental feature that allows contexts and tests to be annotated in order to change their execution.
 */
interface TestAnnotation<F> : NodeTransform {

    /**
     * Used (ironically) to *add* an annotation to a context or test block.
     */
    operator fun <F, NodeBuilderT: NodeBuilder<F>> minus(nodeBuilder: NodeBuilderT): NodeBuilderT =
        nodeBuilder.also {
            it.annotateWith(this as TestAnnotation<in F>)
        }

    /**
     * Groups annotations into a list.
     */
    operator fun plus(that: TestAnnotation<F>) = listOf(this, that)

    override fun <F> transform(node: Node<F>): Node<F> = node
}

/**
 * Add a list of annotations to a context or test block.
 */
operator fun <F, NodeBuilderT: NodeBuilder<F>> Iterable<TestAnnotation<F>>.minus(nodeBuilder: NodeBuilderT): NodeBuilderT =
    nodeBuilder.also { it.annotateWith(this) }

/**
 * Adds an annotation to a context block from the inside.
 */
fun <PF, F> TestContextBuilder<PF, F>.annotateWith(annotation: TestAnnotation<in PF>) {
    (this as NodeBuilder<PF>).annotateWith(annotation)
}

/**
 * For use by annotation transforms to establish if they should.
 */
fun TestAnnotation<*>.appliesTo(node: Node<*>) = node.annotations.contains(this)

fun <F> NodeBuilder<F>.annotateWith(annotations: Iterable<TestAnnotation<in F>>) {
    annotations.forEach {
        this.annotateWith(it)
    }
}
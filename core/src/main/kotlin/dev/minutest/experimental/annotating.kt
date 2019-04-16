package dev.minutest.experimental

import dev.minutest.NodeBuilder
import dev.minutest.NodeTransform
import dev.minutest.TestContextBuilder

/**
 * Used (ironically) to *add* an annotation to a context or test block.
 */
operator fun <F, NodeBuilderT: NodeBuilder<F>> TestAnnotation.minus(nodeBuilder: NodeBuilderT): NodeBuilderT =
    nodeBuilder.also {
        this.applyTo(it)
    }

/**
 * Groups annotations into a list.
 */
operator fun <A: TestAnnotation> A.plus(other: A): List<A> = listOf(this, other)

/**
 * Add a list of annotations to a context or test block.
 */
operator fun <F, NodeBuilderT: NodeBuilder<F>> Iterable<TestAnnotation>.minus(nodeBuilder: NodeBuilderT): NodeBuilderT =
    nodeBuilder.also { it.prependAnnotations(this) }

/**
 * If you are looking at this you are probably trying to add a mismatched set of [TestAnnotation]s to a test or context
 * block. This definition is provided to try to catch this issue by luring the compiler into failure.
 */
operator fun <F> Iterable<*>.minus(@Suppress("UNUSED_PARAMETER") thing: NodeBuilder<*>): Nothing = error("")

/**
 * Adds an annotation to a context block from the inside.
 */
fun <PF, F> TestContextBuilder<PF, F>.annotateWith(annotation: TestAnnotation) {
    @Suppress("UNCHECKED_CAST") // information hiding downcast
    annotation.applyTo(this as NodeBuilder<PF>)
}

/**
 * Adds a transform to a context block from the inside.
 */
fun <PF, F> TestContextBuilder<PF, F>.transformWith(transform: NodeTransform<PF>) {
    @Suppress("UNCHECKED_CAST") // information hiding downcast
    (this as NodeBuilder<PF>).transforms.add(0, transform)
}
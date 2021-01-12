package dev.minutest.experimental

import dev.minutest.Annotatable
import dev.minutest.NodeTransform
import dev.minutest.TestContextBuilder

/**
 * Used (ironically) to *add* an annotation to a context or test block.
 */
operator fun <T: Annotatable<*>> TestAnnotation.minus(annotatable: T): T =
    annotatable.also {
        this.applyTo(it)
    }

/**
 * Groups annotations into a list.
 */
operator fun <A: TestAnnotation> A.plus(other: A): List<A> = listOf(this, other)

/**
 * Add a list of annotations to a context or test block.
 */
operator fun <T: Annotatable<*>> Iterable<TestAnnotation>.minus(annotatable: T): T =
    annotatable.also {
        forEach { annotation ->
            annotation.applyTo(it)
        }
    }

/**
 * Adds an annotation to a context block from the inside.
 */
fun <PF, F> TestContextBuilder<PF, F>.annotateWith(annotation: TestAnnotation) {
    annotation.applyTo(this as Annotatable<*>)
}

/**
 * Adds a transform to a context block from the inside.
 */
fun <PF, F> TestContextBuilder<PF, F>.addTransform(transform: NodeTransform<PF>) {
    @Suppress("UNCHECKED_CAST") // information hiding downcast
    (this as Annotatable<PF>).addTransform(transform)
}
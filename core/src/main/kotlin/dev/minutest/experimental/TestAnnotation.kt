package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeTransform
import dev.minutest.RootTransform

/**
 * An experimental feature that allows contexts and tests to be annotated in order to change their execution.
 *
 * [F] is the type of the fixture for the [Node] that the annotation applies to.
 */
interface TestAnnotation<in F> {

    /**
     * Any [NodeTransform] that this annotation applies.
     *
     * The extra type parameter allows TestAnnotation to be contravariant, whilst NodeTransform is invariant.
     */
    fun <F2: F> transformOfType(): NodeTransform<F2>? = null

    /**
     * Any [RootTransform] that this annotation applies
     */
    val rootTransform: RootTransform? get() = null
}
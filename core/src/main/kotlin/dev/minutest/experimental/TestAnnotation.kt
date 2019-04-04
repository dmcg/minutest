package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeTransform
import dev.minutest.RootTransform

/**
 * An experimental feature that allows contexts and tests to be annotated in order to change their execution.
 *
 * [F] is the type of the fixture of the [Node] that the annotation applies to.
 *
 * An annotation may have a [transform] - if so the transform is applied to the annotated node before tests are run.
 *
 * An annotation may have a [rootTransform] - if so the rootTransform is applied to root of the node tree before
 * tests are run.
 */
interface TestAnnotation<F> {

    /**
     * Any [NodeTransform] that this annotation will apply.
     */
    val transform: NodeTransform<F>? get() = null // *

    /**
     * Any [RootTransform] that this annotation will apply
     */
    val rootTransform: RootTransform? get() = null
}
package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeBuilder
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
interface TestAnnotation<in F> {

    fun applyTo(nodeBuilder: NodeBuilder<@UnsafeVariance F>) {
        nodeBuilder.addAnnotation(this)
    }

    /**
     * Any [RootTransform] that this annotation will apply
     */
    val rootTransform: RootTransform? get() = null


    // * - What I think that the @UnsafeVariance is saying is, yes, you *could* build a TestAnnotation that had a
    // transform that was suss.
    // But if the constructors of TestAnnotation only allow invariant F then this is safe, and they themselves take
    // a NodeTransform, which is just passed back here, so is safe?
}
package dev.minutest.experimental

import dev.minutest.NodeBuilder

/**
 * An experimental feature that allows contexts and tests to be annotated in order to change their execution.
 */
interface TestAnnotation<in F> {

    fun applyTo(nodeBuilder: NodeBuilder<@UnsafeVariance F>) = Unit

    // * - What I think that the @UnsafeVariance is saying is, yes, you *could* build a TestAnnotation that had a
    // transform that was suss.
    // But if the constructors of TestAnnotation only allow invariant F then this is safe, and they themselves take
    // a NodeTransform, which is just passed back here, so is safe?
}
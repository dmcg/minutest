package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeTransform
import dev.minutest.RootTransform

/**
 * An experimental feature that allows contexts and tests to be annotated in order to change their execution.
 *
 * [F] is the type of the fixture of the [Node] that the annotation applies to.
 */
interface TestAnnotation<in F> {

    /**
     * Any [NodeTransform] that this annotation will apply.
     */
    val transform: NodeTransform<@UnsafeVariance F>? get() = null
    // What I think that the @UnsafeVariance is saying is, yes, you *could* build a TestAnnotation that had a transform
    // that was suss.
    // But if the constructors of TestAnnotation only allow invariant F then this is safe, and they themselves take
    // a NodeTransform, which is just passed back here, so is safe?

    /**
     * Any [RootTransform] that this annotation will apply
     */
    val rootTransform: RootTransform? get() = null
}
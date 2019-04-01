package dev.minutest.experimental

import dev.minutest.NodeTransform

/**
 * Convenience implementation of [TestAnnotation].
 */
open class TransformingAnnotation<in F>(
    override val transform: NodeTransform<@UnsafeVariance F>
) : TestAnnotation<F>
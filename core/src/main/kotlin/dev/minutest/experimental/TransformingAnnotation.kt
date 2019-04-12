package dev.minutest.experimental

import dev.minutest.NodeTransform

/**
 * Convenience implementation of [TestAnnotation].
 */
open class TransformingAnnotation<F>(
    override val transform: NodeTransform<F>
) : TestAnnotation<F>
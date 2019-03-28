package dev.minutest.experimental

import dev.minutest.NodeTransform

/**
 * Convenience implementation of [TestAnnotation].
 */
open class TransformingAnnotation<in F>(
    private val transform: NodeTransform<F>
) : TestAnnotation<F> {

    @Suppress("UNCHECKED_CAST") // only slightly suspicious
    override fun <F2 : F> transformOfType() = transform as NodeTransform<F2>
}
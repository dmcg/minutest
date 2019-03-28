package dev.minutest.experimental

import dev.minutest.RootTransform

/**
 * Convenience implementation of [TestAnnotation].
 */
open class RootAnnotation<in T>(
    override val rootTransform: RootTransform
) : TestAnnotation<T>
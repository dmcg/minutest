package dev.minutest.experimental

import dev.minutest.RootTransform

/**
 * Convenience implementation of [TestAnnotation].
 */
open class RootAnnotation<T>(
    override val rootTransform: RootTransform
) : TestAnnotation<T>
package dev.minutest.experimental

import dev.minutest.NodeBuilder
import dev.minutest.RootTransform

/**
 * Convenience implementation of [TestAnnotation].
 */
open class RootAnnotation(
    private val rootTransform: RootTransform
) : TestAnnotation<Any?>, RootTransform by rootTransform {

    override fun applyTo(nodeBuilder: NodeBuilder<Any?>) {
        nodeBuilder.addAnnotation(this)
    }

}
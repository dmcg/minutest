package dev.minutest.experimental

import dev.minutest.Annotatable
import dev.minutest.RootTransform

/**
 * A [TestAnnotation] carrying a [RootTransform] that will be applied before tests are run.
 */
class RootAnnotation(
    private val rootTransform: RootTransform
) : TestAnnotation, RootTransform by rootTransform {

    override fun applyTo(annotatable: Annotatable<*>) {
        annotatable.addMarker(this)
    }
}
package dev.minutest.experimental

import dev.minutest.Annotatable

/**
 * A [TestAnnotation] that just adds itself to the node as marker to be found by later processing.
 */
class MarkerAnnotation : TestAnnotation {
    override fun applyTo(annotatable: Annotatable<*>) {
        annotatable.addMarker(this)
    }
}
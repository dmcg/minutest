package dev.minutest.experimental

import dev.minutest.Node

/**
 * For use by annotation transforms to establish if they should.
 */
fun Node<*>.hasMarker(marker: Any) = markers.contains(marker)

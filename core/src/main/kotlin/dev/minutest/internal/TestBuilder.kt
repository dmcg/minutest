package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.transformedBy

internal data class TestBuilder<F>(
    val name: String,
    val f: (F, TestDescriptor) -> F
) : NodeBuilder<F> {

    private val markers = mutableListOf<Any>()
    private val transforms = mutableListOf<NodeTransform<F>>()

    override fun buildNode() =
        Test(name, markers, NodeId.forBuilder(this), f)
            .transformedBy(transforms)

    override fun addMarker(marker: Any) {
        markers.add(marker)
    }

    override fun addTransform(transform: NodeTransform<F>) {
        transforms.add(transform)
    }
}



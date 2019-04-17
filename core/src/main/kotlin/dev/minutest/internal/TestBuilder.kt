package dev.minutest.internal

import dev.minutest.NodeBuilder
import dev.minutest.NodeTransform
import dev.minutest.Test
import dev.minutest.TestDescriptor
import dev.minutest.experimental.transformedBy

internal data class TestBuilder<F>(val name: String, val f: F.(TestDescriptor) -> F) : NodeBuilder<F> {

    private val annotations: MutableList<Any> = mutableListOf()
    private val transforms: MutableList<NodeTransform<F>> = mutableListOf()

    override fun buildNode() = Test(name, annotations, f).transformedBy(transforms)

    override fun addAnnotation(annotation: Any) {
        annotations.add(annotation)
    }

    override fun addTransform(transform: NodeTransform<F>) {
        transforms.add(0, transform)
    }
}



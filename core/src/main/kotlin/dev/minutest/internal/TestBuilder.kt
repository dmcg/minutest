package dev.minutest.internal

import dev.minutest.TestDescriptor
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.transformedBy

internal data class TestBuilder<F>(val name: String, val f: F.(TestDescriptor) -> F) : dev.minutest.NodeBuilder<F> {

    override val annotations: MutableList<TestAnnotation> = mutableListOf()

    override fun buildNode() = dev.minutest.Test(name, annotations, f).transformedBy(annotations)
}



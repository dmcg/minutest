package dev.minutest.internal

import dev.minutest.NodeBuilder
import dev.minutest.Test
import dev.minutest.TestDescriptor
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.transformedBy

internal data class TestBuilder<F>(val name: String, val f: F.(TestDescriptor) -> F) : NodeBuilder<F> {

    private val annotations: MutableList<TestAnnotation> = mutableListOf()

    override fun buildNode() = Test(name, annotations, f).transformedBy(annotations)

    override fun annotateWith(annotation: TestAnnotation) {
        annotations.add(annotation)
    }
}



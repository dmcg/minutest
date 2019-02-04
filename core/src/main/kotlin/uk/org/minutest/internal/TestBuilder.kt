package uk.org.minutest.internal

import uk.org.minutest.TestDescriptor
import uk.org.minutest.experimental.TestAnnotation
import uk.org.minutest.experimental.transformedBy

internal data class TestBuilder<F>(val name: String, val f: F.(TestDescriptor) -> F) : uk.org.minutest.NodeBuilder<F> {

    override val annotations: MutableList<TestAnnotation> = mutableListOf()

    override fun buildNode() = uk.org.minutest.Test(name, annotations, f).transformedBy(annotations)
}



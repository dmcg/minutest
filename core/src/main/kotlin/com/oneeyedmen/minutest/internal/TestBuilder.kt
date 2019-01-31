package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.experimental.TestAnnotation
import com.oneeyedmen.minutest.experimental.transformedBy

internal data class TestBuilder<F>(val name: String, val f: F.(TestDescriptor) -> F) : NodeBuilder<F> {

    override val annotations: MutableList<TestAnnotation> = mutableListOf()

    override fun buildNode() = Test(name, annotations, f).transformedBy(annotations)
}



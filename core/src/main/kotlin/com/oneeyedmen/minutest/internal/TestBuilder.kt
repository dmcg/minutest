package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.experimental.TestAnnotation

internal data class TestBuilder<F>(val name: String, val f: F.(TestDescriptor) -> F) : NodeBuilder<F> {

    override val annotations: MutableList<TestAnnotation> = mutableListOf()

    override fun buildNode() = RuntimeTest(name, annotations, f)
}
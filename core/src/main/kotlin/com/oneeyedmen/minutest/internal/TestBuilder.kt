package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.TestDescriptor

internal data class TestBuilder<F>(val name: String, val f: F.(TestDescriptor) -> F) : NodeBuilder<F, F> {

    override val properties: MutableMap<Any, Any> = HashMap()

    override fun buildNode(parent: RuntimeContext<F>?) = PreparedRuntimeTest(name, parent, f, properties)
}
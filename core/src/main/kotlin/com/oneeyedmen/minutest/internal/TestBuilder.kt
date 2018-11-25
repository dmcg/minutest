package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeTest

internal data class TestBuilder<F>(val name: String, val f: F.() -> F) : NodeBuilder<F> {

    override val properties: MutableMap<Any, Any> = HashMap()

    override fun buildNode(parent: ParentContext<F>): RuntimeTest =
        PreparedRuntimeTest(name, parent, f, properties)

}
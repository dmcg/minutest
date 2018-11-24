package com.oneeyedmen.minutest.internal

internal data class TestBuilder<F>(val name: String, val f: F.() -> F) : NodeBuilder<F> {
    override fun buildNode(parent: ParentContext<F>) = PreparedRuntimeTest(
        name,
        parent,
        f)
}
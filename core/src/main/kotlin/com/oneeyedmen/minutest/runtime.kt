package com.oneeyedmen.minutest

sealed class RuntimeNode {
    abstract val name: String
    abstract val properties: Map<Any, Any>
}

abstract class RuntimeContext : RuntimeNode(), AutoCloseable {
    abstract val children: List<RuntimeNode>
    abstract fun withChildren(children: List<RuntimeNode>): RuntimeContext
    abstract fun runTest(test: Test<*>, parentFixture: Any, testDescriptor: TestDescriptor): Any
}

data class RuntimeTest(
    override val name: String,
    override val properties: Map<Any, Any>,
    private val f: Test<Any?>
) : RuntimeNode(), Test<Any?> by f

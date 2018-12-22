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

abstract class RuntimeTest: RuntimeNode(), Test<Any?>

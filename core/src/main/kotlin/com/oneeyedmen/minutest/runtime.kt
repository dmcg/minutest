package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.ParentContext

sealed class RuntimeNode {
    abstract val name: String
    abstract val properties: Map<Any, Any>
}

abstract class RuntimeContext : RuntimeNode(), AutoCloseable {
    abstract val children: List<RuntimeNode>
    abstract fun withChildren(children: List<RuntimeNode>): RuntimeContext
    abstract fun runTest(test: Test<*>, parentContext: ParentContext<*>)
}

abstract class RuntimeTest: RuntimeNode() {
    abstract fun run(parentContext: ParentContext<*>)
}

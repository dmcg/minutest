package com.oneeyedmen.minutest

sealed class RuntimeNode : Named {
    abstract override val parent: RuntimeContext<*>?
    abstract val properties: Map<Any, Any>
}

abstract class RuntimeContext<F> : RuntimeNode(), AutoCloseable {
    abstract val children: List<RuntimeNode>
    abstract fun withChildren(children: List<RuntimeNode>): RuntimeContext<F>
    abstract fun runTest(test: Test<F>)
}

abstract class RuntimeTest: RuntimeNode() {
    abstract fun run()
}
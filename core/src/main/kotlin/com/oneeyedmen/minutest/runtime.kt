package com.oneeyedmen.minutest

sealed class RuntimeNode : Named {
    abstract override val parent: RuntimeContext<*>?
    abstract val properties: Map<Any, Any>
    abstract fun adoptedBy(parent: RuntimeContext<*>): RuntimeNode
}

abstract class RuntimeContext<F> : RuntimeNode(), AutoCloseable {
    abstract val children: List<RuntimeNode>
    // TODO - should be withTransformedChildren, taking a (RuntimeContext) -> RuntimeNode
    abstract fun adopting(children: List<RuntimeNode>): RuntimeContext<F>
    abstract override fun adoptedBy(parent: RuntimeContext<*>): RuntimeContext<F>
    abstract fun runTest(test: Test<F>)
}

abstract class RuntimeTest: RuntimeNode() {
    abstract override val parent: RuntimeContext<*>
    abstract fun run()
    abstract override fun adoptedBy(parent: RuntimeContext<*>): RuntimeTest
}
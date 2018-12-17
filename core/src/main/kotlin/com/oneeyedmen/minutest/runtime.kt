package com.oneeyedmen.minutest

sealed class RuntimeNode<PF, F> : Named {
    abstract override val parent: RuntimeContext<*, PF>?
    abstract val properties: Map<Any, Any>
    abstract fun adoptedBy(parent: RuntimeContext<*, PF>): RuntimeNode<PF, F>
}

abstract class RuntimeContext<PF, F> : RuntimeNode<PF, F>(), AutoCloseable {
    abstract val children: List<RuntimeNode<F, *>>
    abstract fun runTest(test: Test<F>)

    abstract fun adopting(children: List<RuntimeNode<F, *>>): RuntimeContext<PF, F>
    abstract override fun adoptedBy(parent: RuntimeContext<*, PF>): RuntimeContext<PF, F>
}

abstract class RuntimeTest<F>: RuntimeNode<F, F>() {
    abstract override val parent: RuntimeContext<*, F>
    abstract fun run()
    abstract override fun adoptedBy(parent: RuntimeContext<*, F>): RuntimeTest<F>
}
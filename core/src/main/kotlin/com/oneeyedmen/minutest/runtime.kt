package com.oneeyedmen.minutest

sealed class RuntimeNode<F> {
    abstract val name: String
    abstract val properties: Map<Any, Any>
}

abstract class RuntimeContext<PF, F> : RuntimeNode<PF>(), AutoCloseable {
    abstract val children: List<RuntimeNode<F>>
    abstract fun withChildren(children: List<RuntimeNode<F>>): RuntimeContext<PF, F>
    abstract fun runTest(test: Test<F>, parentFixture: PF, testDescriptor: TestDescriptor): F
}

data class RuntimeTest<F>(
    override val name: String,
    override val properties: Map<Any, Any>,
    private val f: Test<F>
) : RuntimeNode<F>(), Test<F> by f

package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.Named

sealed class RuntimeNode : Named

abstract class RuntimeContext : RuntimeNode() {
    abstract val children: List<RuntimeNode>
}

abstract class RuntimeTest: RuntimeNode() {
    abstract fun run()
}

/**
 * The runtime representation of a context.
 */
internal data class RuntimeContextWithFixture<PF, F> internal constructor(
    override val name: String,
    override val parent: ParentContext<PF>,
    override val children: List<RuntimeNode>,
    private val operations: Operations<PF, F>
) : RuntimeContext(), ParentContext<F> {

    override fun runTest(test: Test<F>) {
        parent.runTest(operations.buildParentTest(test))
    }
}

/**
 * The runtime representation of a test.
 */
internal class RuntimeTestWithFixture<F> internal constructor(
    override val name: String,
    override val parent: ParentContext<F>,
    private val f: F.() -> F
) : RuntimeTest(), Test<F>, (F)-> F by f {
    override fun run() = parent.runTest(this)
}
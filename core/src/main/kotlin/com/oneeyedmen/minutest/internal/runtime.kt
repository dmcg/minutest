package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.Named

sealed class RuntimeNode : Named

/**
 * The runtime representation of a context.
 */
data class RuntimeContext<PF, F> internal constructor(
    override val name: String,
    override val parent: ParentContext<PF>,
    val children: List<RuntimeNode>,
    private val operations: Operations<PF, F>
) : ParentContext<F>, RuntimeNode() {

    override fun runTest(test: Test<F>) {
        parent.runTest(operations.buildParentTest(test))
    }
}

/**
 * The runtime representation of a test.
 */
class RuntimeTest<F> internal constructor(
    override val name: String,
    override val parent: ParentContext<F>,
    private val f: F.() -> F
) : Test<F>, RuntimeNode(), (F)-> F by f {
    fun run() = parent.runTest(this)
}
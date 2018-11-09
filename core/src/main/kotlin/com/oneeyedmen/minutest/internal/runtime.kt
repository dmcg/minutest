package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test

internal sealed class TestNode

/**
 * The runtime representation of a context.
 */
internal data class RuntimeContext<PF, F>(
    override val name: String,
    override val parent: ParentContext<PF>,
    val children: List<TestNode>,
    private val operations: Operations<PF, F>
) : ParentContext<F>, TestNode() {

    override fun runTest(test: Test<F>) {
        parent.runTest(operations.prepareTest(test))
    }
}

/**
 * The runtime representation of a test.
 */
internal class RuntimeTest<F>(
    override val name: String,
    override val parent: ParentContext<F>,
    private val f: F.() -> F
) : Test<F>, TestNode(), (F)-> F by f {
    fun run() = parent.runTest(this)
}
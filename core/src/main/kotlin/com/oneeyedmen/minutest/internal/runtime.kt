package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.Test

/**
 * The runtime representation of a context.
 */
internal data class RuntimeContextWithFixture<PF, F>(
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
internal class RuntimeTestWithFixture<F>(
    override val name: String,
    override val parent: ParentContext<F>,
    private val f: F.() -> F
) : RuntimeTest(), Test<F>, (F)-> F by f {
    override fun run() = parent.runTest(this)
}
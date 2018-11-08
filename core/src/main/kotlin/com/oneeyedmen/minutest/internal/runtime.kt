package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor

internal sealed class TestNode

/**
 * The runtime representation of a context.
 */
internal data class RuntimeContext<PF, F>(
    override val name: String,
    override val parent: ParentContext<PF>,
    val children: List<TestNode>,
    private val fixtureFactory: (PF, TestDescriptor) -> F,
    private val operations: Operations<F>
) : ParentContext<F>, TestNode() {

    override fun runTest(test: Test<F>) {
        val testWithPreparedFixture = object : Test<F>, Named by test {
            override fun invoke(initialFixture: F) =
                operations.applyBeforesTo(initialFixture)
                    .tryMap(test)
                    .onLastValue(operations::applyAftersTo)
                    .orThrow()
        }
        
        val testInParent = object : Test<PF>, Named by test {
            override fun invoke(parentFixture: PF): PF {
                val transformedTest = operations.applyTransformsTo(testWithPreparedFixture)
                val initialFixture = fixtureFactory(parentFixture, this)
                transformedTest(initialFixture)
                return parentFixture
            }
        }
        
        parent.runTest(testInParent)
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
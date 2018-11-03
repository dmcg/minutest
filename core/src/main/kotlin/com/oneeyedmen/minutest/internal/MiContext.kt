package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.Test

internal data class MiContext<PF, F>(
    override val name: String,
    override val parent: ParentContext<PF>,
    private val fixtureFn: (PF.() -> F),
    private val children: List<Node>,
    private val operations: Operations<F>
) : ParentContext<F>, Node {

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
                val initialFixture = fixtureFn(parentFixture)
                transformedTest(initialFixture)
                return parentFixture
            }
        }
        
        parent.runTest(testInParent)
    }

    override fun toRuntimeNode(): RuntimeContext = RuntimeContext(
        this.name,
        this.children.asSequence().map { it.toRuntimeNode() }
    )
}
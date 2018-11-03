package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.Test

internal class MiContext2<PF, F>(
    override val name: String,
    override val parent: ParentContext<PF>,
    private val fixtureFn: (PF.() -> F),
    var children: List<Node>, // TODO - fix var
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
                val initialFixture = createFixtureFrom(parentFixture)
                transformedTest(initialFixture)
                return parentFixture
            }
        }
        
        parent.runTest(testInParent)
    }
    
    private fun createFixtureFrom(parentFixture: PF): F = fixtureFn(parentFixture)
    
    override fun toRuntimeNode(): RuntimeContext = RuntimeContext(
        this.name,
        this.children.asSequence().map { it.toRuntimeNode() }
    )
}
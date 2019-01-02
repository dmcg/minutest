package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.experimental.TestAnnotation

/**
 * The runtime representation of a context.
 */
internal data class PreparedRuntimeContext<PF, F> (
    override val name: String,
    override val children: List<RuntimeNode<F>>,
    private val befores: List<(F) -> Unit>,
    private val afters: List<(F) -> Unit>,
    private var afterAlls: List<() -> Unit>,
    private val fixtureFactory: (PF, TestDescriptor) -> F,
    override val annotations: MutableList<TestAnnotation>
) : RuntimeContext<PF, F>() {

    override fun runTest(test: Test<F>, parentFixture: PF, testDescriptor: TestDescriptor): F {
        val testWithPreparedFixture: Test<F> = { parentFixture1, testDescriptor1 ->
            applyBeforesTo(parentFixture1)
                .tryMap { f -> test(f, testDescriptor1) }
                .onLastValue(::applyAftersTo)
                .orThrow()
        }
        return testWithPreparedFixture.invoke(fixtureFactory(parentFixture, testDescriptor), testDescriptor)
    }

    override fun close() {
        afterAlls.forEach {
            it()
        }
    }

    private fun applyAftersTo(fixture: F) {
        afters.forEach { afterFn ->
            afterFn(fixture)
        }
    }

    override fun withChildren(children: List<RuntimeNode<F>>) = copy(children = children)

    // apply befores in order - if anything is thrown return it and the last successful value
    private fun applyBeforesTo(fixture: F): OpResult<F> {
        befores.forEach { beforeFn ->
            try {
                beforeFn(fixture)
            } catch (t: Throwable) {
                return OpResult(t, fixture)
            }
        }
        return OpResult(null, fixture)
    }
}
package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.Node
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.Testlet
import com.oneeyedmen.minutest.experimental.TestAnnotation

/**
 * The runtime representation of a context.
 */
internal data class PreparedContext<PF, F> (
    override val name: String,
    override val children: List<Node<F>>,
    private val befores: List<(F, TestDescriptor) -> Unit>,
    private val afters: List<(F, TestDescriptor) -> Unit>,
    private var afterAlls: List<() -> Unit>,
    private val fixtureFactory: (PF, TestDescriptor) -> F,
    override val annotations: MutableList<TestAnnotation>
) : Context<PF, F>() {

    override fun runTest(testlet: Testlet<F>, parentFixture: PF, testDescriptor: TestDescriptor): F {
        val fixture = fixtureFactory(parentFixture, testDescriptor)
        return applyBeforesTo(fixture, testDescriptor)
            .tryMap { f -> testlet(f, testDescriptor) }
            .onLastValue { applyAftersTo(it, testDescriptor) }
            .orThrow()
    }

    override fun close() {
        afterAlls.forEach {
            it()
        }
    }

    private fun applyAftersTo(fixture: F, testDescriptor: TestDescriptor) {
        afters.forEach { afterFn ->
            afterFn(fixture, testDescriptor)
        }
    }

    override fun withChildren(children: List<Node<F>>) = copy(children = children)

    // apply befores in order - if anything is thrown return it and the last successful value
    private fun applyBeforesTo(fixture: F, testDescriptor: TestDescriptor): SequenceResult<F> {
        befores.forEach { beforeFn ->
            try {
                beforeFn(fixture, testDescriptor)
            } catch (t: Throwable) {
                return SequenceResult(t, fixture)
            }
        }
        return SequenceResult(null, fixture)
    }
}
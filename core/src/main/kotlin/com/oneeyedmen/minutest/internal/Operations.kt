package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.TestTransform

internal class Operations<PF, F>(
    var fixtureFactory: ((PF, TestDescriptor) -> F)?
) {
    private val befores = mutableListOf<(F) -> Unit>()
    private val afters = mutableListOf<(F) -> Unit>()
    private val transforms = mutableListOf<TestTransform<F>>()
    private lateinit var resolvedFixtureFactory: ((PF, TestDescriptor) -> F)

    fun addBefore(f: (F) -> Unit) { befores.add(f) }

    fun addAfter(f: (F) -> Unit) { afters.add(f) }

    fun addTransform(transform: TestTransform<F>) { transforms.add(transform) }

    fun buildParentTest(test: Test<F>): Test<PF> {
        val testWithPreparedFixture = object : Test<F>, Named by test {
            override fun invoke(initialFixture: F) =
                applyBeforesTo(initialFixture)
                    .tryMap(test)
                    .onLastValue(::applyAftersTo)
                    .orThrow()
        }

        return object : Test<PF>, Named by test {
            override fun invoke(parentFixture: PF): PF {
                val transformedTest = applyTransformsTo(testWithPreparedFixture)
                transformedTest(resolvedFixtureFactory(parentFixture, this))
                return parentFixture
            }
        }
    }

    // apply befores in order - if anything is thrown return it and the last successful value
    private fun applyBeforesTo(fixture: F): OpResult<F> {
        befores.forEach { beforeFn ->
            try {
                beforeFn(fixture)
            }
            catch (t: Throwable) {
                return OpResult(t, fixture)
            }
        }
        return OpResult(null, fixture)
    }

    private fun applyTransformsTo(test: Test<F>): Test<F> =
        transforms.fold(test) { acc, transform -> transform(acc) }

    private fun applyAftersTo(fixture: F) {
        afters.forEach { afterFn ->
            afterFn(fixture)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun tryToResolveFixtureFactory(noTestsInContext: Boolean, contextName: String) {
        resolvedFixtureFactory = when {
            fixtureFactory != null -> fixtureFactory
            hasNoBeforesOrAfters() && noTestsInContext -> { _, _ -> Unit as F }
            // this is safe provided there are only fixture not replaceFixture calls in sub-contexts,
            // as we cannot provide a fixture here to act as receiver. TODO - check somehow
            else -> error("Fixture has not been set in context \"$contextName\"")
        }!!
    }

    private fun hasNoBeforesOrAfters() = befores.isEmpty() && afters.isEmpty()
}

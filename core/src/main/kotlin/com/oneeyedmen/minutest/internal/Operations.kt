package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.TestTransform

internal class Operations<PF, F>(
    var fixtureFactory: ((PF) -> F)?
) {
    private val befores = mutableListOf<(F) -> Unit>()
    private val afters = mutableListOf<(F) -> Unit>()
    private val transforms = mutableListOf<TestTransform<F>>()
    private lateinit var resolvedFixtureFactory: ((PF) -> F)

    fun addBefore(f: (F) -> Unit) { befores.add(f) }

    fun addAfter(f: (F) -> Unit) { afters.add(f) }

    fun addTransform(transform: TestTransform<F>) { transforms.add(transform) }

    fun hasNoBeforesOrAfters() = befores.isEmpty() && afters.isEmpty()

    // apply befores in order - if anything is thrown return it and the last successful value
    fun applyBeforesTo(fixture: F): OpResult<F> {
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

    fun applyTransformsTo(test: Test<F>): Test<F> =
        transforms.fold(test, { acc, transform -> transform(acc) })

    fun applyAftersTo(fixture: F) {
        afters.forEach { afterFn ->
            afterFn(fixture)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun tryToResolveFixtureFactory(noTestsInContext: Boolean, contextName: String) {
        resolvedFixtureFactory = when {
            fixtureFactory != null -> fixtureFactory
            hasNoBeforesOrAfters() && noTestsInContext -> { _ -> Unit as F }
            // this is safe provided there are only fixture not replaceFixture calls in sub-contexts,
            // as we cannot provide a fixture here to act as receiver. TODO - check somehow
            else -> error("Fixture has not been set in context \"$contextName\"")
        }!!
    }

    fun createFixture(parentFixture: PF) = resolvedFixtureFactory(parentFixture)

    val testDescriptorHolder = TestDescriptorHolder(null)
}

data class TestDescriptorHolder(var testDescriptor: TestDescriptor?) : Named {
    override val name: String
        get() = testDescriptor?.name ?: error("no testDescription set")
    override val parent: Named?
        get() = if (testDescriptor == null) error("no testDescription set") else testDescriptor?.parent
}

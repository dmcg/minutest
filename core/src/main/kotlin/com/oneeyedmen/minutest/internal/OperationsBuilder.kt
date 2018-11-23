package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.TestTransform

internal class OperationsBuilder<PF, F>(
    var fixtureFactory: ((PF, TestDescriptor) -> F)?
) {
    private val befores = mutableListOf<(F) -> Unit>()
    private val afters = mutableListOf<(F) -> Unit>()
    private val transforms = mutableListOf<TestTransform<F>>()

    fun addBefore(f: (F) -> Unit) { befores.add(f) }

    fun addAfter(f: (F) -> Unit) { afters.add(f) }

    fun addTransform(transform: TestTransform<F>) { transforms.add(transform) }

    @Suppress("UNCHECKED_CAST")
    fun buildOrThrow(noTestsInContext: Boolean, contextName: String): Operations<PF, F> {
        val resolvedFixtureFactory = when {
            fixtureFactory != null -> fixtureFactory
            hasNoBeforesOrAfters() && noTestsInContext -> { _, _ -> Unit as F }
            // this is safe provided there are only fixture not replaceFixture calls in sub-contexts,
            // as we cannot provide a fixture here to act as receiver. TODO - check somehow
            else -> error("Fixture has not been set in context \"$contextName\"")
        }!!
        return Operations(befores, afters, transforms, resolvedFixtureFactory)
    }

    private fun hasNoBeforesOrAfters() = befores.isEmpty() && afters.isEmpty()
}


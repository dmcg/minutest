package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.TestTransform

internal class Operations<PF, F>(
    private val befores: List<(F) -> Unit>,
    private val afters: List<(F) -> Unit>,
    private val transforms: List<TestTransform<F>>,
    private val fixtureFactory: ((PF, TestDescriptor) -> F)
) {
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
                transformedTest(fixtureFactory(parentFixture, this))
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
}
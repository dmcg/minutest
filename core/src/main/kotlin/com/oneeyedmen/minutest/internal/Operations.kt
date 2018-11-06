package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.TestTransform

internal class Operations<F> {
    val befores = mutableListOf<(F) -> Unit>()
    val afters = mutableListOf<(F) -> Unit>()
    val transforms = mutableListOf<TestTransform<F>>()

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

    val testDescriptorHolder = TestDescriptorHolder(null)
}

data class TestDescriptorHolder(var testDescriptor: TestDescriptor?) : Named {
    override val name: String
        get() = testDescriptor?.name ?: error("no testDescription set")
    override val parent: Named?
        get() = if (testDescriptor == null) error("no testDescription set") else testDescriptor?.parent
}

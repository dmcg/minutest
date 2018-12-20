package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor

/**
 * The runtime representation of a test.
 */
internal data class PreparedRuntimeTest<F>(
    override val name: String,
    private val f: F.(TestDescriptor) -> F,
    override val properties: Map<Any, Any>
) : RuntimeTest(), Test<F> {
    
    override fun invoke(fixture: F, testDescriptor: TestDescriptor) = fixture.f(testDescriptor)
    
    override fun run(parentContext: ParentContext<*>) {
        (parentContext as ParentContext<F>).runTest(this)
    }

    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)
}

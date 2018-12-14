package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor

/**
 * The runtime representation of a test.
 */
internal data class PreparedRuntimeTest<F>(
    override val name: String,
    override val parent: ParentContext<F>,
    private val f: F.(TestDescriptor) -> F,
    override val properties: Map<Any, Any>
) : RuntimeTest(), Test<F>, (F)-> F {
    
    override fun invoke(fixture: F) = fixture.f(this)
    
    override fun run() = parent.runTest(this)

    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)
}

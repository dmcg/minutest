package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor

/**
 * The runtime representation of a test.
 */
internal class PreparedRuntimeTest<F>(
    override val name: String,
    override val parent: RuntimeContext<*, F>,
    override val properties: Map<Any, Any>,
    private val f: F.(TestDescriptor) -> F
) : RuntimeTest<F>(), Test<F>, (F) -> F {

    override fun invoke(fixture: F) = fixture.f(this)
    
    override fun run() = parent.runTest(this)

    override fun adoptedBy(parent: RuntimeContext<*, F>) = PreparedRuntimeTest(name, parent, properties, f)
}


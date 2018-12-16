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
    parent: RuntimeContext<F>?,
    override val properties: Map<Any, Any>,
    private val f: F.(TestDescriptor) -> F
) : RuntimeTest(), Test<F>, (F) -> F {

    override val parent: RuntimeContext<F> = parent ?: error("RuntimeTest [$name] must have a parent")

    override fun invoke(fixture: F) = fixture.f(this)
    
    override fun run() = parent.runTest(this)

    override fun adoptedBy(parent: RuntimeContext<*>?) = PreparedRuntimeTest(name, parent as RuntimeContext<F>, properties, f)
}


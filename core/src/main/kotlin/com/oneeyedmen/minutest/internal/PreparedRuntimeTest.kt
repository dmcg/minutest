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
    parent: RuntimeContext?,
    private val f: F.(TestDescriptor) -> F,
    override val properties: Map<Any, Any>
) : RuntimeTest(), Test<F>, (F) -> F {

    override val parent: RuntimeContext = parent ?: error("RuntimeTest [$name] must have a parent")

    override fun invoke(fixture: F) = fixture.f(this)
    
    override fun run() = parent.runTest(this)

    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)

    private fun copy(
        name: String = this.name,
        parent: RuntimeContext? = this.parent,
        f:  F.(TestDescriptor) -> F = this.f,
        properties: Map<Any, Any> = this.properties
    ) = PreparedRuntimeTest(name, parent, f, properties)
}

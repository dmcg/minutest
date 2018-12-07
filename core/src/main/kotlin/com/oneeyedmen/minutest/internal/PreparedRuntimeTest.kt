package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.Test

/**
 * The runtime representation of a test.
 */
internal data class PreparedRuntimeTest<F>(
    override val name: String,
    override val parent: ParentContext<F>,
    private val f: F.() -> F,
    override val properties: Map<Any, Any>
) : RuntimeTest(), Test<F>, (F)-> F by f {

    override fun run() = parent.runTest(this)

    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)
}
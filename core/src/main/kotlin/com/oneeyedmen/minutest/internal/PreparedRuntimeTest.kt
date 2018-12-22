package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor

/**
 * The runtime representation of a test.
 */
internal data class PreparedRuntimeTest<F>(
    override val name: String,
    override val properties: Map<Any, Any>,
    private val f: F.(TestDescriptor) -> F
) : RuntimeTest(), Test<Any?> by f as Test<Any?> {
    
    override fun run(parentContext: ParentContext<*>) {
        (parentContext as ParentContext<F>).runTest(f, name)
    }
}

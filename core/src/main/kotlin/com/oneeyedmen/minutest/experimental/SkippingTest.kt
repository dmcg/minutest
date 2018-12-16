package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeTest
import org.opentest4j.TestAbortedException

/**
 * A very special RuntimeTest that does not need a parent, as it never needs a fixture.
 */
internal class SkippingTest<F>(
    override val name: String,
    override val parent: RuntimeContext<F>?,
    override val properties: Map<Any, Any>
) : RuntimeTest() {
    override fun run() = throw TestAbortedException("skipped")
}
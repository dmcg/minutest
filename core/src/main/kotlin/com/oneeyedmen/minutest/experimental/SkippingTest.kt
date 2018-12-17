package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeTest
import org.opentest4j.TestAbortedException

/**
 * A very special RuntimeTest that does not need a parent, as it never needs a fixture.
 */
internal class SkippingTest(
    override val name: String,
    override val parent: RuntimeContext<*>,
    override val properties: Map<Any, Any>
) : RuntimeTest() {
    override fun run() = throw TestAbortedException("skipped")

    override fun adoptedBy(parent: RuntimeContext<*>) = SkippingTest(name, parent, properties)
}
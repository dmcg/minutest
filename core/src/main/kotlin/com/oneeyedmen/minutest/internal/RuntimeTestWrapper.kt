package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeTest

/**
 * Wraps a RuntimeTest so that its execution can be instrumented or influenced.
 *
 * The test's parent is not updated, so that when it is run, it runs as it would have before.
 */
class RuntimeTestWrapper(
    val delegate: RuntimeTest,
    override val name: String = delegate.name,
    val block: (RuntimeTest) -> Unit = { it.run() }
) : RuntimeTest() {
    override val parent: RuntimeContext<*> = delegate.parent
    override val properties: Map<Any, Any> = delegate.properties

    override fun run() {
        block(delegate)
    }

    override fun adoptedBy(parent: RuntimeContext<*>) = RuntimeTestWrapper(delegate.adoptedBy(parent))

}
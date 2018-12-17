package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeTest

/**
 * Wraps a RuntimeTest so that its execution can be instrumented or influenced.
 *
 * The test's parent is not updated, so that when it is run, it runs as it would have before.
 */
class RuntimeTestWrapper<F>(
    val delegate: RuntimeTest<F>,
    override val name: String = delegate.name,
    val block: (RuntimeTest<F>) -> Unit = { it.run() }
) : RuntimeTest<F>() {
    override val parent: RuntimeContext<*, F> = delegate.parent
    override val properties: Map<Any, Any> = delegate.properties

    override fun run() {
        block(delegate)
    }

    override fun adoptedBy(parent: RuntimeContext<*, F>) = RuntimeTestWrapper(delegate.adoptedBy(parent))

}
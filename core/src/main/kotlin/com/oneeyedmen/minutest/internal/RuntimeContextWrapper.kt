package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.Test

/**
 * Wraps a RuntimeContext so that its execution can be instrumented or influenced.
 *
 * The context's parent is not updated, so that when it is run, it runs as it would have before.
 */
open class RuntimeContextWrapper<F>(
    val wrapped: RuntimeContext<F>,
    override val children: List<RuntimeNode> = wrapped.children,
    override val name: String = wrapped.name
) : RuntimeContext<F>() {

    override val parent = wrapped.parent
    override val properties = wrapped.properties

    override fun runTest(test: Test<F>) = wrapped.runTest(test)

    override fun adopting(children: List<RuntimeNode>) = wrapped.adopting(children.map { it.adoptedBy(this) })
    override fun adoptedBy(parent: RuntimeContext<*>?) = wrapped.adoptedBy(parent)

    override fun close() = wrapped.close()
}
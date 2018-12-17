package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.Test

/**
 * Wraps a RuntimeContext so that its execution can be instrumented or influenced.
 *
 * The context's parent is not updated, so that when it is run, it runs as it would have before.
 */
open class RuntimeContextWrapper<PF, F>(
    val wrapped: RuntimeContext<PF, F>,
    override val children: List<RuntimeNode<F, *>> = wrapped.children,
    override val name: String = wrapped.name
) : RuntimeContext<PF, F>() {

    override val parent = wrapped.parent
    override val properties = wrapped.properties

    override fun runTest(test: Test<F>) = wrapped.runTest(test)

    override fun adopting(children: List<RuntimeNode<F, *>>) = wrapped.adopting(children.map { it.adoptedBy(this) })
    override fun adoptedBy(parent: RuntimeContext<*, PF>) = wrapped.adoptedBy(parent)

    override fun close() = wrapped.close()
}
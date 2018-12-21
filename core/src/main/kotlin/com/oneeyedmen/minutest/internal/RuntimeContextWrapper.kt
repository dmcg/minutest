package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.Test

internal data class RuntimeContextWrapper(
    override val name: String,
    override val properties: Map<Any, Any>,
    override val children: List<RuntimeNode>,
    val f: (Test<*>, ParentContext<*>) -> Unit,
    val onClose: () -> Unit
) : RuntimeContext() {
    override fun runTest(test: Test<*>, parentContext: ParentContext<*>) = f(test, parentContext)

    constructor(
        delegate: RuntimeContext,
        name: String = delegate.name,
        properties: Map<Any, Any> = delegate.properties,
        children: List<RuntimeNode> = delegate.children,
        runner: (Test<*>, ParentContext<*>) -> Unit = delegate::runTest,
        onClose: () -> Unit = delegate::close
        ) : this(name, properties, children, runner, onClose)

    override fun withChildren(children: List<RuntimeNode>) = copy(children = children)

    override fun close() = onClose.invoke()
}
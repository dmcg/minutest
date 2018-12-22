package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor

internal data class RuntimeContextWrapper(
    override val name: String,
    override val properties: Map<Any, Any>,
    override val children: List<RuntimeNode>,
    val f2: (Test<*>, parentFixture: Any, TestDescriptor) -> Any,
    val onClose: () -> Unit
) : RuntimeContext() {
    override fun runTest(test: Test<*>, parentFixture: Any, testDescriptor: TestDescriptor): Any =
        f2(test, parentFixture, testDescriptor)

    constructor(
        delegate: RuntimeContext,
        name: String = delegate.name,
        properties: Map<Any, Any> = delegate.properties,
        children: List<RuntimeNode> = delegate.children,
        f2: (Test<*>, parentFixture: Any, TestDescriptor) -> Any = delegate::runTest,
        onClose: () -> Unit = delegate::close
        ) : this(name, properties, children, f2, onClose)

    override fun withChildren(children: List<RuntimeNode>) = copy(children = children)

    override fun close() = onClose.invoke()
}
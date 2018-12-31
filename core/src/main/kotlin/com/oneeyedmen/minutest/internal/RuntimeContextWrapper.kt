package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.experimental.TestAnnotation

internal data class RuntimeContextWrapper<PF, F>(
    override val name: String,
    override val annotations: List<TestAnnotation>,
    override val children: List<RuntimeNode<F>>,
    val runner: (Test<F>, parentFixture: PF, TestDescriptor) -> F,
    val onClose: () -> Unit
) : RuntimeContext<PF, F>() {
    constructor(
        delegate: RuntimeContext<PF, F>,
        name: String = delegate.name,
        properties: List<TestAnnotation> = delegate.annotations,
        children: List<RuntimeNode<F>> = delegate.children,
        runner: (Test<F>, parentFixture: PF, TestDescriptor) -> F = delegate::runTest,
        onClose: () -> Unit = delegate::close
        ) : this(name, properties, children, runner, onClose)

    override fun runTest(test: Test<F>, parentFixture: PF, testDescriptor: TestDescriptor): F =
        runner(test, parentFixture, testDescriptor)

    override fun withChildren(children: List<RuntimeNode<F>>) = copy(children = children)

    override fun close() = onClose.invoke()
}
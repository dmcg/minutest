package dev.minutest.internal

import dev.minutest.NodeTransform
import dev.minutest.TestDescriptor
import dev.minutest.Testlet
import dev.minutest.applyTo
import dev.minutest.experimental.TestAnnotation

internal data class ContextWrapper<PF, F>(
    override val name: String,
    override val annotations: List<TestAnnotation>,
    override val children: List<dev.minutest.Node<F>>,
    val runner: (Testlet<F>, parentFixture: PF, TestDescriptor) -> F,
    val onClose: () -> Unit
) : dev.minutest.Context<PF, F>() {

    constructor(
        delegate: dev.minutest.Context<PF, F>,
        name: String = delegate.name,
        properties: List<TestAnnotation> = delegate.annotations,
        children: List<dev.minutest.Node<F>> = delegate.children,
        runner: (Testlet<F>, parentFixture: PF, TestDescriptor) -> F = delegate::runTest,
        onClose: () -> Unit = delegate::close
        ) : this(name, properties, children, runner, onCloseFor(delegate, onClose))



    override fun runTest(testlet: Testlet<F>, parentFixture: PF, testDescriptor: TestDescriptor): F =
        runner(testlet, parentFixture, testDescriptor)

    override fun withTransformedChildren(transform: NodeTransform) = copy(children = transform.applyTo(children))


    override fun close() = onClose.invoke()
}

// We always want to call close on the delegate, but only once
private fun <PF, F> onCloseFor(delegate: dev.minutest.Context<PF, F>, specified: () -> Unit): () -> Unit =
    if (specified == delegate::close) specified else {
        {
            delegate.close()
            specified()
        }
    }
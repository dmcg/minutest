package uk.org.minutest.internal

import uk.org.minutest.NodeTransform
import uk.org.minutest.TestDescriptor
import uk.org.minutest.Testlet
import uk.org.minutest.applyTo
import uk.org.minutest.experimental.TestAnnotation

internal data class ContextWrapper<PF, F>(
    override val name: String,
    override val annotations: List<TestAnnotation>,
    override val children: List<uk.org.minutest.Node<F>>,
    val runner: (Testlet<F>, parentFixture: PF, TestDescriptor) -> F,
    val onClose: () -> Unit
) : uk.org.minutest.Context<PF, F>() {

    constructor(
        delegate: uk.org.minutest.Context<PF, F>,
        name: String = delegate.name,
        properties: List<TestAnnotation> = delegate.annotations,
        children: List<uk.org.minutest.Node<F>> = delegate.children,
        runner: (Testlet<F>, parentFixture: PF, TestDescriptor) -> F = delegate::runTest,
        onClose: () -> Unit = delegate::close
        ) : this(name, properties, children, runner, onCloseFor(delegate, onClose))



    override fun runTest(testlet: Testlet<F>, parentFixture: PF, testDescriptor: TestDescriptor): F =
        runner(testlet, parentFixture, testDescriptor)

    override fun withTransformedChildren(transform: NodeTransform) = copy(children = transform.applyTo(children))


    override fun close() = onClose.invoke()
}

// We always want to call close on the delegate, but only once
private fun <PF, F> onCloseFor(delegate: uk.org.minutest.Context<PF, F>, specified: () -> Unit): () -> Unit =
    if (specified == delegate::close) specified else {
        {
            delegate.close()
            specified()
        }
    }
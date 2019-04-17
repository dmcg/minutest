package dev.minutest


/**
 * [Node]s form a tree of [Context]s and [Test]s.
 *
 * [F] is the type of the fixture that will be supplied *to* the node.
 */
sealed class Node<F> {
    abstract val name: String
    internal abstract val markers: List<Any>
}

/**
 * A container for [Node]s, which are accessed as [Context.children].
 *
 * [PF] is the parent fixture type, which will be supplied to the context.
 * [F] is the fixture type of the children - the context will supply this to them.
 */
abstract class Context<PF, F> : Node<PF>(), AutoCloseable {
    abstract val children: List<Node<F>>

    /**
     * Invoke a [Testlet], converting a parent fixture [PF] to the type required by the test.
     */
    abstract fun runTest(testlet: Testlet<F>, parentFixture: PF, testDescriptor: TestDescriptor): F

    /**
     * Return a copy of this node with children transformed.
     */
    internal abstract fun withTransformedChildren(transform: NodeTransform<F>): Context<PF, F>
}

/**
 * A [Node] that represents an executable test.
 */
data class Test<F>(
    override val name: String,
    override val markers: List<Any>,
    private val f: Testlet<F>
) : Node<F>(), Testlet<F> by f
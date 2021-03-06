package dev.minutest

import dev.minutest.internal.translatingAssumptions

/**
 * [Node]s form a tree of [Context]s and [Test]s.
 *
 * [F] is the type of the fixture that will be supplied *to* the node.
 */
sealed class Node<F> {
    abstract val name: String
    internal abstract val markers: List<Any>

    // The id should be passed through any wrappers around this node
    abstract val id: NodeId
}

/**
 * A container for [Node]s, which are accessed as [Context.children].
 *
 * [PF] is the parent fixture type, which will be supplied to the context.
 * [F] is the fixture type of the children - the context will supply this to them.
 */
abstract class Context<PF, F> : Node<PF>() {
    abstract val children: List<Node<F>>

    /**
     * Invoke a [Testlet], converting a parent fixture [PF] to the type required by the test.
     * Note that the [Testlet] may represent a [Test], or code that a sub-context wants run.
     */
    abstract fun runTest(testlet: Testlet<F>, parentFixture: PF, testDescriptor: TestDescriptor): F

    /**
     * Return a copy of this node with children transformed.
     */
    internal abstract fun withTransformedChildren(transform: NodeTransform<F>): Context<PF, F>

    abstract fun open(testDescriptor: TestDescriptor)
    abstract fun close(testDescriptor: TestDescriptor)
}

/**
 * A [Node] that represents an executable test.
 */
data class Test<F>(
    override val name: String,
    override val markers: List<Any>,
    override val id: NodeId,
    private val f: Testlet<F>
) : Node<F>(),
    Testlet<F> by f.translatingAssumptions()


data class NodeId(val value: Int) {
    companion object {
        internal fun forBuilder(o: Any): NodeId {
            return NodeId(System.identityHashCode(o))
        }
    }
}
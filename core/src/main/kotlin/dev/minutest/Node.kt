package dev.minutest

import dev.minutest.experimental.TestAnnotation


/**
 * [Node]s form a tree of [Context]s and [Test]s.
 *
 * The generic type [F] is the type of the fixture that will be supplied *to* the node.
 */
sealed class Node<in F> {
    abstract val name: String
    internal abstract val annotations: List<TestAnnotation<in F>>
}

/**
 * A container for [Node]s, which are accessed as [Context.children].
 *
 * The generic type [PF] is the parent fixture type. [F] is the type of the children.
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
    internal abstract fun withTransformedChildren(transform: NodeTransform<in F>): Context<PF, F>
}

/**
 * A [Node] that represents an executable test.
 */
data class Test<F>(
    override val name: String,
    override val annotations: List<TestAnnotation<in F>>,
    private val f: Testlet<F>
) : Node<F>(), Testlet<F> by f
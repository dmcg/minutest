package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.experimental.TestAnnotation


/**
 * [Node]s form a tree of [Context]s and [Test]s.
 *
 * The generic type [F] is the type of the fixture that will be supplied *to* the node.
 */
sealed class Node<F> {
    abstract val name: String
    abstract val annotations: List<TestAnnotation>

    /**
     * Return a copy of this node with any children transformed.
     */
    abstract fun withTransformedChildren(transform: NodeTransform): Node<F>
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
}

/**
 * A [Node] that represents an executable test.
 */
data class Test<F>(
    override val name: String,
    override val annotations: List<TestAnnotation>,
    private val f: Testlet<F>
) : Node<F>(), Testlet<F> by f {
    
    override fun withTransformedChildren(transform: NodeTransform) = this
}
package dev.minutest.internal

import dev.minutest.*

/**
 * A [Context] that is a number of otherwise root contexts, smooshed together.
 *
 * Used where we find more than one TestFactory in a JUnit5 test, or to collect all
 * the tests in a package using the MinutestTestEngine.
 *
 * Maybe it shouldn't exist - the RunnableNode might be able to do the job.
 */
internal data class AmalgamatedRootContext(
    override val name: String,
    override val markers: List<Any> = emptyList(),
    val stork: () -> List<Node<Unit>>
) : Context<Unit, Unit>() {

    override val children by lazy(stork)

    override fun runTest(
        testlet: Testlet<Unit>,
        parentFixture: Unit,
        testDescriptor: TestDescriptor
    ) {
        // We don't have any befores, afters or fixtures, so we can just run with Unit
        testlet.invoke(Unit, testDescriptor)
    }

    fun withFilteredChildren(predicate: (Node<Unit>) -> Boolean) =
        this.copy(stork = { stork().filter(predicate) })

    override fun withTransformedChildren(transform: NodeTransform<Unit>)
        : Context<Unit, Unit> {
        // We shouldn't be able to call this
        TODO("not implemented")
    }

    override fun open(testDescriptor: TestDescriptor) {
        // we shouldn't be holding any resources
    }

    override fun close(testDescriptor: TestDescriptor) {
        // we shouldn't be holding any resources
    }

    override val id get() = NodeId.forBuilder(this)
}
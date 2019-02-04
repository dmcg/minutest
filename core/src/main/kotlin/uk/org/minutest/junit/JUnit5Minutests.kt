
package uk.org.minutest.junit

import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import uk.org.minutest.internal.RootExecutor
import uk.org.minutest.internal.TestExecutor
import uk.org.minutest.internal.TopLevelContextBuilder
import java.util.stream.Stream
import kotlin.streams.asStream

interface JUnit5Minutests {

    val tests: TopLevelContextBuilder<*>? get() = null // a clue to what to override

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun tests(): Stream<out DynamicNode> = tests.let { testsFromVal ->
        when  {
            testsFromVal != null -> testsFromVal.buildNode().toStreamOfDynamicNodes(RootExecutor)
            else -> this.rootContextFromMethods().toStreamOfDynamicNodes(RootExecutor)
        }
    }
}

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory].
 *
 * @see [NodeBuilder<Unit>#testFactory()]
 */
fun <F> testFactoryFor(root: TopLevelContextBuilder<F>) = root.buildNode().toStreamOfDynamicNodes(RootExecutor)

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory]
 *
 * @see [testFactoryFor(NodeBuilder<Unit>)]
 */
fun <F> TopLevelContextBuilder<F>.toTestFactory() = testFactoryFor(this)

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

private fun <F> uk.org.minutest.Node<F>.toStreamOfDynamicNodes(executor: TestExecutor<F>) = when (this) {
    is uk.org.minutest.Context<F, *> -> this.toStreamOfDynamicNodes(executor)
    is uk.org.minutest.Test<F> -> Stream.of(this.toDynamicNode(executor))
}

private fun <PF, F> uk.org.minutest.Context<PF, F>.toStreamOfDynamicNodes(executor: TestExecutor<PF>): Stream<out DynamicNode> =
    children.toStreamOfDynamicNodes(this, executor.andThen(this))

private fun <F> Iterable<uk.org.minutest.Node<F>>.toStreamOfDynamicNodes(parent: uk.org.minutest.Context<*, F>, executor: TestExecutor<F>) =
    asSequence()
        .map { it.toDynamicNode(executor) }
        .asStream()
        .onClose { parent.close() }

private fun <F> uk.org.minutest.Node<F>.toDynamicNode(executor: TestExecutor<F>): DynamicNode = when (this) {
    is uk.org.minutest.Test<F> -> dynamicTest(name) {
        executor.runTest(this)
    }
    is uk.org.minutest.Context<F, *> -> dynamicContainer(name, this.toStreamOfDynamicNodes(executor))
}




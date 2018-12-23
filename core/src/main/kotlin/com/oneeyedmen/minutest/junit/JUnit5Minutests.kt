
package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.RootExecutor
import com.oneeyedmen.minutest.internal.TestExecutor
import com.oneeyedmen.minutest.internal.TopLevelContextBuilder
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
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

private fun <PF, F> RuntimeContext<PF, F>.toStreamOfDynamicNodes(executor: TestExecutor<PF>): Stream<out DynamicNode> =
    children.toStreamOfDynamicNodes(this, executor.andThen(this))

private fun <F> Iterable<RuntimeNode<F, *>>.toStreamOfDynamicNodes(parent: RuntimeContext<*, F>, executor: TestExecutor<F>) =
    asSequence()
        .map { it.toDynamicNode(executor) }
        .asStream()
        .onClose { parent.close() }

private fun <PF, F> RuntimeNode<PF, F>.toDynamicNode(executor: TestExecutor<PF>): DynamicNode = when (this) {
    is RuntimeTest<*> -> dynamicTest(name) {
        executor.runTest(this as RuntimeTest<PF>)
    }
    is RuntimeContext -> dynamicContainer(name, this.toStreamOfDynamicNodes(executor))
}




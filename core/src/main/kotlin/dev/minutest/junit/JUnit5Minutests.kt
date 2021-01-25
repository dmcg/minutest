package dev.minutest.junit

import dev.minutest.Context
import dev.minutest.RootContextBuilder
import dev.minutest.internal.rootContextFromMethods
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.util.stream.Stream

/**
 * Mix-in this interface to run your tests with JUnit 5
 */
interface JUnit5Minutests {

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    @Execution(ExecutionMode.SAME_THREAD) // we want to control parallel
        // execution _within_ a context
    fun minutests(): Stream<DynamicNode> =
        rootContextFromMethods()
            ?.toRootStreamOfDynamicNodes()
            ?: error("No test methods found")
}

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory].
 *
 * @see [RootContextBuilder#testFactory()]
 */
fun testFactoryFor(
    root: RootContextBuilder,
): Stream<DynamicNode> =
    (root.buildNode() as? Context<Unit, *>)?.toRootStreamOfDynamicNodes()
        ?: error("Root node was a test, not a context\n" +
            "This can happen if you skip on the root context, sorry.")

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory]
 *
 * @see [testFactoryFor(RootContextBuilder)]
 */
fun RootContextBuilder.toTestFactory(
): Stream<DynamicNode> =
    testFactoryFor(this)
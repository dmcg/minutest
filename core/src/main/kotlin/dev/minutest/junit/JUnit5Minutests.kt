package dev.minutest.junit

import dev.minutest.RootContextBuilder
import dev.minutest.internal.rootContextFromMethods
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
    fun minutests(): Iterable<DynamicNode> =
        rootContextFromMethods()
            .toRootListOfDynamicNodes(Executors.newFixedThreadPool(10))
}

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory].
 *
 * @see [RootContextBuilder#testFactory()]
 */
fun testFactoryFor(
    root: RootContextBuilder,
    executorService: ExecutorService? = null
): Iterable<DynamicNode> =
    root.buildNode().toRootListOfDynamicNodes(executorService)

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory]
 *
 * @see [testFactoryFor(RootContextBuilder)]
 */
fun RootContextBuilder.toTestFactory(
    executorService: ExecutorService? = null
): Iterable<DynamicNode> =
    testFactoryFor(this, executorService)
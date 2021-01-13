package dev.minutest.junit

import dev.minutest.RootContextBuilder
import dev.minutest.internal.rootContextFromMethods
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory

/**
 * Mix-in this interface to run your tests with JUnit 5
 */
interface JUnit5Minutests {

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun minutests(): Iterable<DynamicNode> =
        rootContextFromMethods()
            .toRootListOfDynamicNodes()
}

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory].
 *
 * @see [RootContextBuilder#testFactory()]
 */
fun testFactoryFor(root: RootContextBuilder): Iterable<DynamicNode> =
    root.buildNode().toRootListOfDynamicNodes()

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory]
 *
 * @see [testFactoryFor(RootContextBuilder)]
 */
fun RootContextBuilder.toTestFactory(): Iterable<DynamicNode> =
    testFactoryFor(this)
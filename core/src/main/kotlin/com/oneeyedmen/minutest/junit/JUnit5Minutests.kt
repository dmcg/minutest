
package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.TopLevelContextBuilder
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
            testsFromVal != null -> testsFromVal.buildRootNode().toStreamOfDynamicNodes()
            else -> this.rootContextFromMethods().toStreamOfDynamicNodes()
        }
    }
}

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory].
 *
 * @see [NodeBuilder<Unit>#testFactory()]
 */
fun testFactoryFor(root: TopLevelContextBuilder<*>) = root.buildRootNode().toStreamOfDynamicNodes()

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory]
 *
 * @see [testFactoryFor(NodeBuilder<Unit>)]
 */
fun TopLevelContextBuilder<*>.toTestFactory() = testFactoryFor(this)

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

private fun RuntimeContext.toStreamOfDynamicNodes(): Stream<out DynamicNode> = children.toStreamOfDynamicNodes(this)

private fun RuntimeNode.toDynamicNode(): DynamicNode = when (this) {
    is RuntimeTest -> dynamicTest(name) { this.run() }
    is RuntimeContext -> dynamicContainer(name, this.toStreamOfDynamicNodes())
}

private fun Iterable<RuntimeNode>.toStreamOfDynamicNodes(parent: RuntimeContext) =
    asSequence().map(RuntimeNode::toDynamicNode).asStream().onClose { parent.close() }
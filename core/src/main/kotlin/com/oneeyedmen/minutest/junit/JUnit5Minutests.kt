
package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.*
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.streams.asStream

interface JUnit5Minutests {

    val tests: NodeBuilder<Unit, *>? get() = null // a clue to what to override

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun tests(): Stream<out DynamicNode> = when {
        tests != null -> tests!!.buildRootNode().toStreamOfDynamicNodes()
        else -> this.rootContextFromMethods().toStreamOfDynamicNodes()
    }
}

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory].
 *
 * @see [NodeBuilder<Unit>#testFactory()]
 */
fun testFactoryFor(root: NodeBuilder<Unit, *>) = root.buildRootNode().toStreamOfDynamicNodes()

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory]
 *
 * @see [testFactoryFor(NodeBuilder<Unit>)]
 */
fun NodeBuilder<Unit, *>.toTestFactory() = testFactoryFor(this)

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

fun RuntimeNode.toStreamOfDynamicNodes(): Stream<out DynamicNode> =
    if (this is RuntimeContext<*>)
    // don't create a vestigial single-child context
        this.children.toStreamOfDynamicNodes(this)
    else
        Stream.of(this.toDynamicNode())

private fun RuntimeNode.toDynamicNode(): DynamicNode = when (this) {
    is RuntimeTest -> DynamicTest.dynamicTest(name) { this.run() }
    is RuntimeContext<*> -> this.toDynamicContainer()
}

private fun RuntimeContext<*>.toDynamicContainer(): DynamicContainer =
    DynamicContainer.dynamicContainer(name, children.toStreamOfDynamicNodes(this))

private fun Iterable<RuntimeNode>.toStreamOfDynamicNodes(parent: RuntimeContext<*>) =
    asSequence().map(RuntimeNode::toDynamicNode).asStream().onClose { parent.close() }
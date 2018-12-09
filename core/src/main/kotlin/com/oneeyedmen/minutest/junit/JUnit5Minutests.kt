
package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.*
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.streams.asStream

@Deprecated("JupiterTests is now JUnit5Minutests", replaceWith = ReplaceWith("JUnit5Minutests"))
typealias JupiterTests = JUnit5Minutests

interface JUnit5Minutests : JUnitXMinutests {

    override val tests: NodeBuilder<Unit> // a clue to what to override

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun tests(): Stream<out DynamicNode> = tests.buildRootNode().toStreamOfDynamicNodes()
}

/**
 * Define a [Context] and map it to be used as a JUnit [org.junit.jupiter.api.TestFactory].
 *
 * Designed to be called inside a class and to use the name as the class as the name of the test.
 */
@Deprecated("Use testFactoryFor(com.oneeyedmen.minutest.junit.context {})")
inline fun <reified F> Any.junitTests(
    noinline transform: (RuntimeNode) -> RuntimeNode = { it },
    noinline builder: Context<Unit, F>.() -> Unit
): Stream<out DynamicNode> = this.context(transform, builder).toTestFactory()

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory].
 *
 * @see [NodeBuilder<Unit>#testFactory()]
 */
fun testFactoryFor(root: NodeBuilder<Unit>) = root.buildRootNode().toStreamOfDynamicNodes()

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory]
 *
 * @see [testFactoryFor(NodeBuilder<Unit>)]
 */
fun NodeBuilder<Unit>.toTestFactory() = testFactoryFor(this)

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

fun RuntimeNode.toStreamOfDynamicNodes(): Stream<out DynamicNode> =
    if (this is RuntimeContext)
    // don't create a vestigial single-child context
        this.children.toStreamOfDynamicNodes(this)
    else
        Stream.of(this.toDynamicNode())

private fun RuntimeNode.toDynamicNode(): DynamicNode = when (this) {
    is RuntimeTest -> DynamicTest.dynamicTest(name) { this.run() }
    is RuntimeContext -> this.toDynamicContainer()
}

private fun RuntimeContext.toDynamicContainer(): DynamicContainer =
    DynamicContainer.dynamicContainer(name, children.toStreamOfDynamicNodes(this))

private fun Iterable<RuntimeNode>.toStreamOfDynamicNodes(parent: RuntimeContext) =
    asSequence().map(RuntimeNode::toDynamicNode).asStream().onClose { parent.close() }
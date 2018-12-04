package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.transformedTopLevelContext
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.stream.Stream
import kotlin.streams.asStream


/**
 * Define a [Context] and map it to be used as a JUnit [org.junit.jupiter.api.TestFactory].
 *
 * Designed to be called inside a class and to use the name as the class as the name of the test.
 */
inline fun <reified F> Any.junitTests(
    transform: (RuntimeNode) -> RuntimeNode = { it },
    noinline builder: Context<Unit, F>.() -> Unit
): Stream<out DynamicNode> =
    transformedTopLevelContext(javaClass.canonicalName, transform, builder)
        .toStreamOfDynamicNodes()


// These are defined as extensions to avoid taking a dependency on JUnit in the main package

fun RuntimeNode.toStreamOfDynamicNodes(): Stream<out DynamicNode> =
    if (this is RuntimeContext)
        // don't create a vestigial single-child context
        this.children.toStreamOfDynamicNodes(this)
    else
        Stream.of(this.toDynamicNode())

private fun RuntimeNode.toDynamicNode(): DynamicNode = when (this) {
    is RuntimeTest -> dynamicTest(name) { this.run() }
    is RuntimeContext -> this.toDynamicContainer()
}

private fun RuntimeContext.toDynamicContainer(): DynamicContainer =
    dynamicContainer(name, children.toStreamOfDynamicNodes(this))

private fun Iterable<RuntimeNode>.toStreamOfDynamicNodes(parent: RuntimeContext) =
    asSequence().map(RuntimeNode::toDynamicNode).asStream().onClose { parent.close() }
package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.*
import com.oneeyedmen.minutest.internal.askType
import com.oneeyedmen.minutest.internal.topLevelContext
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.stream.Stream


/**
 * Define a [Context] and map it to be used as a JUnit [org.junit.jupiter.api.TestFactory].
 *
 * Designed to be called inside a class and to use the name as the class as the name of the test.
 */
inline fun <reified F> Any.junitTests(
    transform: (RuntimeNode) -> RuntimeNode = { it },
    noinline builder: Context<Unit, F>.() -> Unit
): Stream<out DynamicNode> =
    topLevelContext(javaClass.canonicalName, askType<F>(), builder)
        .buildRootNode()
        .run(transform)
        .toStreamOfDynamicNodes()

inline fun <reified F> Any.junitTests(
    fixture: F,
    transform: (RuntimeNode) -> RuntimeNode = { it },
    noinline builder: Context<Unit, F>.() -> Unit
): Stream<out DynamicNode> =
    topLevelContext(javaClass.canonicalName, askType<F>(), fixture, builder)
        .buildRootNode()
        .run(transform)
        .toStreamOfDynamicNodes()

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

fun RuntimeNode.toStreamOfDynamicNodes(): Stream<out DynamicNode> = Stream.of(this.toDynamicNode())

private fun RuntimeNode.toDynamicNode(): DynamicNode = when (this) {
    is RuntimeTest -> dynamicTest(name) { this.run() }
    is RuntimeContext -> this.toDynamicContainer()
}

private fun RuntimeContext.toDynamicContainer(): DynamicContainer =
    dynamicContainer(name, children.map(RuntimeNode::toDynamicNode))
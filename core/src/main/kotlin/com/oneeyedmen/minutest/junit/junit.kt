package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.asKType
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
inline fun <reified F> Any.junitTests(noinline builder: Context<Unit, F>.() -> Unit): Stream<out DynamicNode> =
    topLevelContext(javaClass.canonicalName, asKType<F>(), builder).toStreamOfDynamicNodes()

inline fun <reified F> Any.junitTests(fixture: F,
    noinline builder: Context<Unit, F>.() -> Unit
): Stream<out DynamicNode> =
    topLevelContext(javaClass.canonicalName, asKType<F>(), fixture, builder).toStreamOfDynamicNodes()

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

fun RuntimeNode.toStreamOfDynamicNodes(): Stream<out DynamicNode> = Stream.of(this.toDynamicNode())

private fun RuntimeNode.toDynamicNode(): DynamicNode = when (this) {
    is RuntimeTest -> dynamicTest(name) { this.run() }
    is RuntimeContext -> this.toDynamicContainer()
}

private fun RuntimeContext.toDynamicContainer(): DynamicContainer =
    dynamicContainer(name, children.map(RuntimeNode::toDynamicNode))

// Will be removed next release

/**
 * Define a [Context] and map it to be used as a JUnit [org.junit.jupiter.api.TestFactory].
 *
 * @see [Any.junitTests]
 */
@Deprecated("Replace with Any.junitTests", replaceWith = ReplaceWith("Any.junitTests"))
inline fun <reified F> junitTestsNamed(name: String,
    noinline builder: Context<Unit, F>.() -> Unit
): Stream<out DynamicNode> =
    topLevelContext(name, asKType<F>(), builder).toStreamOfDynamicNodes()

@Deprecated("Replace with Any.junitTests", replaceWith = ReplaceWith("Any.junitTests"))
inline fun <reified F> junitTestsNamed(name: String,
    fixture: F,
    noinline builder: Context<Unit, F>.() -> Unit
): Stream<out DynamicNode> =
    topLevelContext(name, asKType<F>(), fixture, builder).toStreamOfDynamicNodes()

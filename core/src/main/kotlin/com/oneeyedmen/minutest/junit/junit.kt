package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.internal.*
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.stream.Stream

/**
 * Define a [Context] and map it to be used as a JUnit [org.junit.jupiter.api.TestFactory].
 *
 * @see [Any.junitTests]
 */
inline fun <reified F> junitTestsNamed(name: String, noinline builder: Context<Unit, F>.() -> Unit): Stream<out DynamicNode> =
    topLevelContext(name, F::class.isInstance(Unit), builder).toStreamOfDynamicNodes()

inline fun <reified F> junitTestsNamed(name: String, fixture: F, noinline builder: Context<Unit, F>.() -> Unit): Stream<out DynamicNode> =
    topLevelContext(name, fixture, builder).toStreamOfDynamicNodes()


/**
 * Define a [Context] and map it to be used as a JUnit [org.junit.jupiter.api.TestFactory].
 *
 * Designed to be called inside a class and to use the name as the class as the name of the test.
 */
inline fun <reified F> Any.junitTests(noinline builder: Context<Unit, F>.() -> Unit): Stream<out DynamicNode> =
    junitTestsNamed(javaClass.canonicalName, builder)

inline fun <reified F> Any.junitTests(fixture: F, noinline builder: Context<Unit, F>.() -> Unit): Stream<out DynamicNode> =
    junitTestsNamed(javaClass.canonicalName, fixture, builder)



// These are defined as extensions to avoid taking a dependency on JUnit in the main package

// Note that we take the children of the root context to remove an unnecessary layer. Hence the rootContextName
// is not shown in the test runner. But see ruling.kt - ruleApplyingTest
fun <F> Context<Unit, F>.toStreamOfDynamicNodes(): Stream<out DynamicNode> =
    (this as ContextBuilder<Unit, F>)
        .toTestNode(RootContext)
        .toDynamicContainer()
        .children

private fun TestNode.toDynamicNode(): DynamicNode = when (this) {
    is MinuTest<*> -> dynamicTest(name) { this.run() }
    is MiContext<*, *> -> this.toDynamicContainer()
}

private fun MiContext<*, *>.toDynamicContainer(): DynamicContainer =
    dynamicContainer(name, children.map(TestNode::toDynamicNode))
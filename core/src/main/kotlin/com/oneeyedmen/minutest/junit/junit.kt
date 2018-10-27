package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.*
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.stream.Stream
import kotlin.streams.asStream

/**
 * Define a [TestContext] and map it to be used as a JUnit [org.junit.jupiter.api.TestFactory].
 */
fun <F> Any.junitTests(builder: TestContext<Unit, F>.() -> Unit): Stream<out DynamicNode> =
    junitTestsNamed(javaClass.canonicalName, null, builder)

fun Any.fixturelessJunitTests(builder: TestContext<Unit, Unit>.() -> Unit) =
    junitTestsNamed(javaClass.canonicalName, { Unit }, builder)

fun <F> junitTestsNamed(
    parentContextName: String,
    fixtureFn: (Unit.() -> F)? = null,
    builder: TestContext<Unit, F>.() -> Unit
): Stream<out DynamicNode> =
    topContext(parentContextName, fixtureFn, builder)
        .toRuntimeNode()
        .toDynamicContainer()
        .children

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

// Note that we take the children of the root context to remove an unnecessary layer. Hence the rootContextName
// is not shown in the test runner. But see ruling.kt - ruleApplyingTest
internal fun <F> MiContext<*, F>.toDynamicNodes(): Stream<out DynamicNode> =
    toRuntimeNode()
        .toDynamicContainer()
        .children

private fun RuntimeNode.toDynamicNode(): DynamicNode = when (this) {
    is RuntimeTest -> this.toDynamicTest()
    is RuntimeContext -> this.toDynamicContainer()
}

private fun RuntimeTest.toDynamicTest(): DynamicTest = dynamicTest(name) { this.block() }

private fun RuntimeContext.toDynamicContainer(): DynamicContainer = dynamicContainer(
    name,
    children.map { it.toDynamicNode() }.asStream()
)
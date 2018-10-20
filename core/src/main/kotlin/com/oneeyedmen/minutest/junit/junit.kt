package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.*
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.stream.Stream
import kotlin.reflect.KType
import kotlin.streams.asStream

/**
 * Define a [TestContext] and map it to be used as a JUnit [org.junit.jupiter.api.TestFactory].
 */
inline fun <reified F> junitTests(noinline builder: TestContext<F>.() -> Unit): Stream<out DynamicNode> =
    junitTests(F::class.asKType(null is F), builder)

fun <F> junitTests(fixtureType: KType, builder: TestContext<F>.() -> Unit): Stream<out DynamicNode>
    = MiContext<Unit, F>(rootContextName, null, fixtureType).apply { builder() }.toDynamicNodes()


// Note that we take the children of the root context to remove an unnecessary layer. Hence the rootContextName
// is not shown in the test runner. But see ruling.kt - ruleApplyingTest
internal fun <F> MiContext<*, F>.toDynamicNodes(): Stream<out DynamicNode> =
    toRuntimeNode(null, Operations.empty())
        .toDynamicContainer()
        .children

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

private fun RuntimeNode.toDynamicNode(): DynamicNode = when (this) {
    is RuntimeTest -> this.toDynamicTest()
    is RuntimeContext -> this.toDynamicContainer()
}

private fun RuntimeTest.toDynamicTest(): DynamicTest = dynamicTest(name) { this.block() }

private fun RuntimeContext.toDynamicContainer(): DynamicContainer = dynamicContainer(
    name,
    children.map { it.toDynamicNode() }.asStream()
)

internal const val rootContextName = "ignored"


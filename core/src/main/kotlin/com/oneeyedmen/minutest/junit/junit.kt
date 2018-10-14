package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.*
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.streams.asStream

/**
 * Define a [TestContext] and map it to be used as a JUnit [org.junit.jupiter.api.TestFactory].
 */
inline fun <reified F : Any> junitTests(noinline builder: TestContext<F>.() -> Unit): Stream<out DynamicNode> =
    junitTests(F::class, builder)

fun <F : Any> junitTests(fixtureType: KClass<F>, builder: TestContext<F>.() -> Unit): Stream<out DynamicNode> =
    MiContext("ignored", fixtureType).apply { builder() }
        .toRuntimeNode(null, Operations.empty())
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


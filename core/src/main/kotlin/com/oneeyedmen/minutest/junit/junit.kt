package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.MiContext
import com.oneeyedmen.minutest.internal.MinuTest
import com.oneeyedmen.minutest.internal.Node
import com.oneeyedmen.minutest.internal.Operations
import com.oneeyedmen.minutest.miContext
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.streams.asStream

/**
 * Define a [TestContext] and map it to be used as a JUnit [org.junit.jupiter.api.TestFactory].
 */
inline fun <reified F: Any> junitTests(noinline builder: TestContext<F>.() -> Unit): Stream<out DynamicNode> =
    junitTests(F::class, builder)

fun <F : Any> junitTests(fixtureType: KClass<F>, builder: TestContext<F>.() -> Unit) =
    (miContext("ignored", fixtureType, builder) as MiContext<F>).toDynamicContainer(Operations.empty()).children

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

private fun <F: Any> MiContext<F>.toDynamicContainer(parentOperations: Operations<F>): DynamicContainer = dynamicContainer(name,
    children.asSequence().map { it.toDynamicNode(this, parentOperations) }.asStream())

private fun <F: Any> Node<F>.toDynamicNode(miContext: MiContext<F>, parentOperations: Operations<F>) = when (this) {
    is MinuTest<F> -> this.toDynamicTest(miContext, parentOperations)
    is MiContext<F> -> this.toDynamicContainer(parentOperations + miContext.operations)
}

private fun <F : Any> MinuTest<F>.toDynamicTest(miContext: MiContext<F>, parentOperations: Operations<F>) =
    DynamicTest.dynamicTest(name) { miContext.runTest(this, parentOperations) }

package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.TestContext2
import com.oneeyedmen.minutest.internal.MiContext
import com.oneeyedmen.minutest.internal.MinuTest
import com.oneeyedmen.minutest.internal.Node
import com.oneeyedmen.minutest.internal.Operations
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.stream.Stream
import kotlin.reflect.KProperty1
import kotlin.streams.asStream

/**
 * Define a [TestContext] and map it to be used as a JUnit [TestFactory].
 */
fun <F> junitTests(builder: TestContext<F>.() -> Unit): Stream<out DynamicNode> =
    (rootContext("ignored", builder) as MiContext<Unit, F>).build(Operations.empty()).children

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

private fun <PF, F> MiContext<PF, F>.build(parentOperations: Operations<F>): DynamicContainer = dynamicContainer(name,
    children.asSequence().map { dynamicNodeFor(it, parentOperations) }.asStream())

private fun <PF, F> MiContext<PF, F>.dynamicNodeFor(
    node: Node<F>,
    parentOperations: Operations<F>
) = when (node) {
    is MinuTest<F> -> DynamicTest.dynamicTest(node.name) { runTest(node, parentOperations) }
    is MiContext<*, *> -> (node as MiContext<PF, F>).build(parentOperations + operations)
}

/**
 * Apply a JUnit test rule in a fixture.
 */
fun <T, R: TestRule> TestContext2<*, T>.applyRule(property: KProperty1<T, R>) {
    addTransform { test: Test<T> ->
        MinuTest(test.name) {
            this.also { fixture ->
                val statement = object : Statement() {
                    override fun evaluate() {
                        test(this@MinuTest)
                    }
                }
                property.get(fixture).apply(statement, Description.createTestDescription("GENERATED TEST", name))
            }
        }
    }
}
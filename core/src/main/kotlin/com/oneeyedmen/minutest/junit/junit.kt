package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestContext
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
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.streams.asStream

/**
 * Define a [TestContext] and map it to be used as a JUnit [TestFactory].
 */
fun <F> junitTests(builder: TestContext<F>.() -> Unit): Stream<out DynamicNode> =
    (rootContext("ignored", builder) as MiContext<F>).build(Operations.empty()).children

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

private fun <F> MiContext<F>.build(parentOperations: Operations<F>): DynamicContainer = dynamicContainer(name,
    children.asSequence().map { dynamicNodeFor(it, parentOperations) }.asStream())

private fun <F> MiContext<F>.dynamicNodeFor(
    node: Node<F>,
    parentOperations: Operations<F>
) = when (node) {
    is MinuTest<F> -> DynamicTest.dynamicTest(node.name) { runTest(node, parentOperations) }
    is MiContext<F> -> node.build(parentOperations + operations)
}

/**
 * Apply a JUnit test rule in a fixture.
 */
inline fun <reified F, R: TestRule> TestContext<F>.applyRule(ruleAsFixtureProperty: KProperty1<F, R>) {
    addTransform { test: Test<F> ->
        wrappedTest(test, ruleAsFixtureProperty, name, F::class)
    }
}

fun <F, R : TestRule> wrappedTest(
    test: Test<F>,
    ruleAsFixtureProperty: KProperty1<F, R>,
    contextName: String,
    fixtureClass: KClass<*>
): Test<F> = MinuTest(test.name) {
    this.also { fixture ->
        val rule = ruleAsFixtureProperty.get(fixture)
        val wrappedTestAsStatement = test.asJUnitStatement(fixture)
        rule.apply(
            wrappedTestAsStatement,
            Description.createTestDescription(fixtureClass.java, "$contextName->${test.name}")).evaluate()
    }
}

private fun <F> Test<F>.asJUnitStatement(fixture: F) = object : Statement() {
    override fun evaluate() {
        this@asJUnitStatement(fixture)
    }
}
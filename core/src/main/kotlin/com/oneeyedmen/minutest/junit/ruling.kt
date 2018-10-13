package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.MinuTest
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Apply a JUnit test rule in a fixture.
 */
inline fun <reified F: Any, R: TestRule> TestContext<F>.applyRule(ruleAsFixtureProperty: KProperty1<F, R>) {
    addTransform { test: Test<F> ->
        wrappedTest(test, ruleAsFixtureProperty, name, F::class)
    }
}

fun <F: Any, R : TestRule> wrappedTest(
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
            Description.createTestDescription(
                fixtureClass.java,
                "$contextName->${test.name}")).evaluate()
    }
}

private fun <F: Any> Test<F>.asJUnitStatement(fixture: F) = object : Statement() {
    override fun evaluate() {
        this@asJUnitStatement(fixture)
    }
}
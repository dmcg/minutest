package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.fullName
import org.junit.rules.TestRule
import org.junit.runner.Description.createTestDescription
import org.junit.runners.model.Statement

/**
 * Apply a JUnit test rule in a fixture
 */
inline fun <reified F : Any, R : TestRule> TestContext<F>.applyRule(
    noinline ruleExtractor: F.() -> R
) {
    addTransform { test: Test<F> ->
        ruleApplyingTest(test, ruleExtractor)
    }
}

fun <F : Any, R : TestRule> ruleApplyingTest(
    test: Test<F>,
    ruleExtractor: (F) -> R
): Test<F> =
    object : Test<F>, Named by test {
        override fun invoke(fixture: F) =
            fixture.also {
                val rule = ruleExtractor(fixture)
                val wrappedTestAsStatement = test.asJUnitStatement(fixture)
                val fullName = test.fullName()
                rule.apply(
                    wrappedTestAsStatement,
                    createTestDescription(fullName.first(), fullName.drop(1).joinToString("."))
                ).evaluate()
            }
    }

private fun <F : Any> Test<F>.asJUnitStatement(fixture: F) = object : Statement() {
    override fun evaluate() {
        this@asJUnitStatement(fixture)
    }
}

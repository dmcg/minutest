package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.BaseContext
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.internal.MinuTest
import org.junit.rules.TestRule
import org.junit.runner.Description.createTestDescription
import org.junit.runners.model.Statement
import kotlin.reflect.KClass

/**
 * Apply a JUnit test rule in a fixture
 */
inline fun <reified F : Any, R : TestRule> BaseContext<F>.applyRule(
    testClass: KClass<*>? = null,
    noinline ruleExtractor: F.() -> R
) {
    addTransform { test: Test<F> ->
        ruleApplyingTest(test, ruleExtractor, this, testClass ?: F::class)
    }
}

/**
 * Apply a JUnit test rule in a fixture
 */
inline fun <reified F : Any, R : TestRule> BaseContext<F>.applyRule(noinline ruleExtractor: F.() -> R) =
    applyRule(null, ruleExtractor)

fun <F : Any, R : TestRule> ruleApplyingTest(
    test: Test<F>,
    ruleExtractor: F.() -> R,
    context: BaseContext<F>,
    fixtureClass: KClass<*>
): Test<F> = MinuTest(test.name) {
    this.also { fixture ->
        val rule = ruleExtractor()
        val wrappedTestAsStatement = test.asJUnitStatement(fixture)
        rule.apply(
            wrappedTestAsStatement,
            createTestDescription(
                fixtureClass.java,
                testPath(context, test).joinToString("->")
            )
        ).evaluate()
    }
}

private fun <F : Any> Test<F>.asJUnitStatement(fixture: F) = object : Statement() {
    override fun evaluate() {
        this@asJUnitStatement(fixture)
    }
}

private fun testPath(context: BaseContext<*>, test: Test<*>): List<String> =
    (generateSequence(context) { it.parent }.withoutTopLevelContext().toList().reversed().map(BaseContext<*>::name) + test.name)

// Because junitTests removes the root context when passing to JUnit, the top level context is not seen. So remove it
// from the contexts for consistency
private fun Sequence<BaseContext<*>>.withoutTopLevelContext() = this.takeWhile { !(it.parent == null && it.name == rootContextName) }

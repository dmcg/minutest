package dev.minutest.junit.experimental

import dev.minutest.*
import dev.minutest.experimental.ContextWrapper
import dev.minutest.experimental.addTransform
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.Description.createTestDescription
import org.junit.runners.model.Statement

/**
 * Apply a JUnit test rule in a fixture
 */
fun <PF, F, R : TestRule> TestContextBuilder<PF, F>.applyRule(ruleExtractor: F.() -> R) {
    addTransform(
        TestRuleTransform(ruleExtractor)
    )
}

private class TestRuleTransform<PF, F, R : TestRule>(
    private val ruleExtractor: F.() -> R
) : NodeTransform<PF> {

    override fun invoke(node: Node<PF>): Node<PF> =
        when (node) {
            is Context<PF, *> -> {
                @Suppress("UNCHECKED_CAST") // safe as long as F is not exposed in TestContextBuilder directly, might do better?
                ContextWrapper(node as Context<PF, F>, runner = magicRunnerFor(node))
            }
            else -> error("TestRuleTransform should only be applicable to a context - please report")
        }

    private fun magicRunnerFor(context: Context<PF, F>) =
        fun(testlet: Testlet<F>, parentFixture: PF, testDescriptor: TestDescriptor) =
            context.runTest(magicTestlet(testlet), parentFixture, testDescriptor)

    /**
     * Returns a [Testlet] that
     * 1. Intercepts the fixture and gets the TestRule from it
     * 2. Executes the TestRule around a Statement representing the original Testlet [wrapped]
     * 3. Returns the value returned by [wrapped]
     */
    private fun magicTestlet(wrapped: Testlet<F>) = fun(fixture: F, testDescriptor: TestDescriptor): F {

        var fixtureHolder: List<F>? = null

        val wrappedTestAsStatement = object : Statement() {
            override fun evaluate() {
                fixtureHolder = listOf(wrapped(fixture, testDescriptor))
            }
        }

        ruleExtractor(fixture)
            .apply(wrappedTestAsStatement, testDescriptor.toTestDescription()) // this is TestRule.apply
            .evaluate()

        fixtureHolder?.let {
            return it.first() // you can't lift the return out
        } ?: throw IllegalStateException("fixture not initialised after TestRule evaluation")
    }

    private fun TestDescriptor.toTestDescription(): Description = fullName().let {
        createTestDescription(it.first(), it.drop(1).joinToString("."))
    }
}

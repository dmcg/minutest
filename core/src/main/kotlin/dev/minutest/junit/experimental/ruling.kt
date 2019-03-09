package dev.minutest.junit.experimental

import dev.minutest.*
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.annotateWith
import org.junit.rules.TestRule
import org.junit.runner.Description.createTestDescription
import org.junit.runners.model.Statement

/**
 * Apply a JUnit test rule in a fixture
 */
fun <PF, F, R : TestRule> TestContextBuilder<PF, F>.applyRule(ruleExtractor: F.() -> R) {
    annotateWith(
        object : TestAnnotation<PF> {
            override fun <F2: PF> transformOfType(): NodeTransform<F2> = NodeTransform { node ->
                when (node) {
                    is Test<F2> -> {
                        Test(node.name, node.annotations) { fixture, testDescriptor ->
                            fixture.also {
                                val rule = ruleExtractor(fixture as F)
                                val wrappedTestAsStatement = node.asJUnitStatement(fixture, testDescriptor)
                                val fullName = testDescriptor.fullName()
                                rule.apply(
                                    wrappedTestAsStatement,
                                    createTestDescription(fullName.first(), fullName.drop(1).joinToString("."))
                                ).evaluate()
                            }
                        }
                    }
                    is Context<F2, *> ->
                        node.withTransformedChildren(this.transformOfType<F2>() as NodeTransform<Any?>)
                }
            }
        })
}

private fun <F> Testlet<F>.asJUnitStatement(fixture: F, testDescriptor: TestDescriptor) = object : Statement() {
    override fun evaluate() {
        this@asJUnitStatement(fixture, testDescriptor)
    }
}

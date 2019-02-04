package uk.org.minutest.junit.experimental

import org.junit.rules.TestRule
import org.junit.runner.Description.createTestDescription
import org.junit.runners.model.Statement
import uk.org.minutest.NodeTransform
import uk.org.minutest.TestDescriptor
import uk.org.minutest.Testlet
import uk.org.minutest.experimental.TestAnnotation
import uk.org.minutest.experimental.annotateWith

/**
 * Apply a JUnit test rule in a fixture
 */
fun <F, R : TestRule> uk.org.minutest.ContextBuilder<F>.applyRule(ruleExtractor: F.() -> R) {
    annotateWith(
        ApplyRule(ruleExtractor)
    )
}

// TODO - I think this will fail if you change the fixture type between declaration and the test
class ApplyRule<F, R : TestRule>(private val ruleExtractor: F.() -> R) : TestAnnotation, NodeTransform {
    override fun <F2> applyTo(node: uk.org.minutest.Node<F2>): uk.org.minutest.Node<F2> = when (node) {
        is uk.org.minutest.Test<F2> -> {
            uk.org.minutest.Test(node.name, node.annotations) { fixture, testDescriptor ->
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
        is uk.org.minutest.Context<F2, *> ->
            node.withTransformedChildren(this)
    }
}


private fun <F> Testlet<F>.asJUnitStatement(fixture: F, testDescriptor: TestDescriptor) = object : Statement() {
    override fun evaluate() {
        this@asJUnitStatement(fixture, testDescriptor)
    }
}

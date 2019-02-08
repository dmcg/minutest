package dev.minutest.junit.experimental

import dev.minutest.NodeTransform
import dev.minutest.TestDescriptor
import dev.minutest.Testlet
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.annotateWith
import org.junit.rules.TestRule
import org.junit.runner.Description.createTestDescription
import org.junit.runners.model.Statement

/**
 * Apply a JUnit test rule in a fixture
 */
fun <F, R : TestRule> dev.minutest.ContextBuilder<F>.applyRule(ruleExtractor: F.() -> R) {
    annotateWith(
        ApplyRule(ruleExtractor)
    )
}

// TODO - I think this will fail if you change the fixture type between declaration and the test
class ApplyRule<F, R : TestRule>(private val ruleExtractor: F.() -> R) : TestAnnotation, NodeTransform {
    override fun <F2> applyTo(node: dev.minutest.Node<F2>): dev.minutest.Node<F2> = when (node) {
        is dev.minutest.Test<F2> -> {
            dev.minutest.Test(node.name, node.annotations) { fixture, testDescriptor ->
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
        is dev.minutest.Context<F2, *> ->
            node.withTransformedChildren(this)
    }
}


private fun <F> Testlet<F>.asJUnitStatement(fixture: F, testDescriptor: TestDescriptor) = object : Statement() {
    override fun evaluate() {
        this@asJUnitStatement(fixture, testDescriptor)
    }
}

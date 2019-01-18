package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeNodeTransform
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.experimental.TestAnnotation
import com.oneeyedmen.minutest.experimental.annotateWith
import org.junit.rules.TestRule
import org.junit.runner.Description.createTestDescription
import org.junit.runners.model.Statement

/**
 * Apply a JUnit test rule in a fixture
 */
fun <F, R : TestRule> TestContext<F>.applyRule(ruleExtractor: F.() -> R) {
    annotateWith(
        ApplyRule(ruleExtractor)
    )
}

// TODO - I think this will fail if you change the fixture type between declaration and the test
class ApplyRule<F, R : TestRule>(private val ruleExtractor: F.() -> R) : TestAnnotation, RuntimeNodeTransform {
    override fun <F2> applyTo(node: RuntimeNode<F2>): RuntimeNode<F2> = when (node) {
        is RuntimeTest<F2> -> {
            RuntimeTest(node.name, node.annotations) { fixture, testDescriptor ->
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
        is RuntimeContext<F2, *> ->
            node.withTransformedChildren(this)
    }
}


private fun <F> Test<F>.asJUnitStatement(fixture: F, testDescriptor: TestDescriptor) = object : Statement() {
    override fun evaluate() {
        this@asJUnitStatement(fixture, testDescriptor)
    }
}

package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import kotlin.test.fail


class AnnotationTests : JupiterTests {

    override val tests = SKIP - context<Unit>(skipAndFocus) {
        isNodeBuilder(MyAnnotation - test("single annotation") {})
        isNodeBuilder(MyAnnotation - context("single annotation") {})

        isNodeBuilder(MyAnnotation + AnotherAnnotation - test("2 annotations") {})
        isNodeBuilder(MyAnnotation + AnotherAnnotation + YetAnotherAnnotation - test("3 annotations") {})
        isNodeBuilder(listOf(MyAnnotation, AnotherAnnotation, YetAnotherAnnotation) - test("3 annotations") {})

        context("context") {
            annotateWith(MyAnnotation)
        }
        test("top level skip works") {
            fail("top level skip didn't work")
        }
    }

}

object MyAnnotation : TestAnnotation
object AnotherAnnotation : TestAnnotation
object YetAnotherAnnotation : TestAnnotation

// check that expression is a nodebuilder at compile time
private fun <F> isNodeBuilder(nodeBuilder: NodeBuilder<F>) = nodeBuilder

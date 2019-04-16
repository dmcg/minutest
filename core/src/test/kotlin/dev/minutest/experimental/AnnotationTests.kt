package dev.minutest.experimental

import dev.minutest.NodeBuilder
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Test as JUnitTest

class AnnotationTests : JUnit5Minutests {

    fun tests() = rootContext<Unit> {

        isNodeBuilder(test("no annotations") {})
        isNodeBuilder(unitAnnotation - test("single annotation") {})
        isNodeBuilder(unitAnnotation - context("single annotation") {
            test("in single annotation") {}
        })

        isNodeBuilder(unitAnnotation + anyAnnotation - test("2 annotations") {})
        isNodeBuilder(anyAnnotation + unitAnnotation - test("2 annotations again") {})

        isNodeBuilder(unitAnnotation + anyAnnotation + yetAnotherAnnotation - test("3 annotations") {})
        isNodeBuilder(listOf(unitAnnotation, anyAnnotation, yetAnotherAnnotation) - test("3 annotations again") {})

        context("annotate with") {
            annotateWith(unitAnnotation)
            test("in annotate with") {}
        }

        checkedAgainst(
            "root",
            "  no annotations",
            "  single annotation",
            "    in single annotation",
            "  2 annotations",
            "  2 annotations again",
            "  3 annotations",
            "  3 annotations again",
            "  annotate with",
            "    in annotate with"
        )
    }

    private val unitAnnotation = MarkerAnnotation()
    private val anyAnnotation = MarkerAnnotation()
    private val yetAnotherAnnotation = MarkerAnnotation()
}

// check that expression is a nodebuilder at compile time
fun <F> isNodeBuilder(nodeBuilder: NodeBuilder<F>) = nodeBuilder

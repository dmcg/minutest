package dev.minutest.experimental

import dev.minutest.Annotatable
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext

class AnnotationTests : JUnit5Minutests {

    fun tests() = rootContext {

        isAnnotatable(test("no annotations") {})
        isAnnotatable(unitAnnotation - test("single annotation test") {})
        isAnnotatable(unitAnnotation - context("single annotation context") {
            test("test in single annotation context") {}
        })

        isAnnotatable(unitAnnotation + anyAnnotation - test("2 annotations") {})
        isAnnotatable(anyAnnotation + unitAnnotation - test("2 annotations again") {})

        isAnnotatable(unitAnnotation + anyAnnotation + yetAnotherAnnotation - test("3 annotations") {})
        isAnnotatable(listOf(unitAnnotation, anyAnnotation, yetAnotherAnnotation) - test("3 annotations again") {})

        context("annotate with") {
            annotateWith(unitAnnotation)
            test("in annotate with") {}
        }

        checkedAgainst(
            "root",
            "  no annotations",
            "  single annotation test",
            "  single annotation context",
            "    test in single annotation context",
            "  2 annotations",
            "  2 annotations again",
            "  3 annotations",
            "  3 annotations again",
            "  annotate with",
            "    in annotate with",
            logger = noSymbolsLogger()
        )
    }

    private val unitAnnotation = MarkerAnnotation()
    private val anyAnnotation = MarkerAnnotation()
    private val yetAnotherAnnotation = MarkerAnnotation()
}

// check that expression is a Annotatable at compile time
private fun <F> isAnnotatable(nodeBuilder: Annotatable<F>) = nodeBuilder

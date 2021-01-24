package dev.minutest.experimental

import dev.minutest.Annotatable
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2

class AnnotationTests : JUnit5Minutests {

    fun tests() = rootContext {

        isAnnotatable(test2("no annotations") {})
        isAnnotatable(unitAnnotation - test2("single annotation test") {})
        isAnnotatable(unitAnnotation - context("single annotation context") {
            test2("test in single annotation context") {}
        })

        isAnnotatable(unitAnnotation + anyAnnotation - test2("2 annotations") {})
        isAnnotatable(anyAnnotation + unitAnnotation - test2("2 annotations again") {})

        isAnnotatable(unitAnnotation + anyAnnotation + yetAnotherAnnotation - test2("3 annotations") {})
        isAnnotatable(listOf(unitAnnotation, anyAnnotation, yetAnotherAnnotation) - test2("3 annotations again") {})

        context("annotate with") {
            annotateWith(unitAnnotation)
            test2("in annotate with") {}
        }

        checkedAgainst(
            "tests",
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

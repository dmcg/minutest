package dev.minutest.experimental

import dev.minutest.NodeBuilder
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Test as JUnitTest

class AnnotationTests : JUnit5Minutests {

    fun tests() = rootContext<Unit> {

        isNodeBuilder(test("no annotations") {})
        isNodeBuilder(UnitAnnotation - test("single annotation") {})
        isNodeBuilder(UnitAnnotation - context("single annotation") {
            test("in single annotation") {}
        })

        isNodeBuilder(UnitAnnotation + AnyAnnotation - test("2 annotations") {})
        isNodeBuilder(AnyAnnotation + UnitAnnotation - test("2 annotations again") {})

        isNodeBuilder(UnitAnnotation + AnyAnnotation + YetAnotherAnnotation - test("3 annotations") {})
        isNodeBuilder(listOf(UnitAnnotation, AnyAnnotation, YetAnotherAnnotation) - test("3 annotations again") {})

        context("annotate with") {
            annotateWith(UnitAnnotation)
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

    object UnitAnnotation : TestAnnotation<Unit>
    object AnyAnnotation : TestAnnotation<Any?>
    object YetAnotherAnnotation : TestAnnotation<Unit>
}

// check that expression is a nodebuilder at compile time
fun <F> isNodeBuilder(nodeBuilder: NodeBuilder<F>) = nodeBuilder

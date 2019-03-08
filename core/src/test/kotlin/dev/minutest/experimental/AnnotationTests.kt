package dev.minutest.experimental

import dev.minutest.*
import org.junit.jupiter.api.Test


class AnnotationTests {

    val log = mutableListOf<String>()


    @Test fun tests() {
        val tests = rootContext<Unit>(loggedTo(log)) {
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
        }
        checkLog(tests,
            "▾ root",
            "  ✓ no annotations",
            "  ✓ single annotation",
            "    ✓ in single annotation",
            "  ✓ 2 annotations",
            "  ✓ 2 annotations again",
            "  ✓ 3 annotations",
            "  ✓ 3 annotations again",
            "  ▾ annotate with",
            "    ✓ in annotate with")
    }

    private fun scopeForStaticTypeChecking() {

        rootContext<Number> {
            fixture { 42 }
            isNodeBuilder(NumberAnnotation - test("") {})
            isNodeBuilder(AnyAnnotation - test("") {})

            isNodeBuilder(AnyAnnotation + NumberAnnotation - test("") {})

            // Don't compile
//            IntAnnotation - test("") {}
//            UnitAnnotation - test("") {}
//            StringAnnotation - test("") {}
        }

        rootContext<Int> {
            fixture { 42 }
            isNodeBuilder(NumberAnnotation - test("") {})
            isNodeBuilder(AnyAnnotation - test("") {})
            isNodeBuilder(IntAnnotation - test("") {})

            isNodeBuilder(AnyAnnotation + NumberAnnotation - test("") {})
            isNodeBuilder(AnyAnnotation + NumberAnnotation + IntAnnotation - test("") {})

            // Don't compile
//            UnitAnnotation - test("") {}
//            StringAnnotation - test("") {}
//            AnyAnnotation + NumberAnnotation + StringAnnotation - test("") {}
        }
    }


    private fun checkLog(tests: RootContextBuilder<*>, vararg expected: String) {
        executeTests(tests)
        assertLogged(log, *expected)
    }
}

object UnitAnnotation : TestAnnotation<Unit>
object AnyAnnotation : TestAnnotation<Any?>
object YetAnotherAnnotation : TestAnnotation<Unit>
object NumberAnnotation : TestAnnotation<Number>
object IntAnnotation : TestAnnotation<Int>
object StringAnnotation : TestAnnotation<String>

// check that expression is a nodebuilder at compile time
private fun <F> isNodeBuilder(nodeBuilder: NodeBuilder<F>) = nodeBuilder

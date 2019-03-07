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
            isNodeBuilder(UnitAnnotation + AnyAnnotation + YetAnotherAnnotation - test("3 annotations") {})

            // This would be nice, but variance isn't right yet
            // isNodeBuilder(AnyAnnotation + UnitAnnotation - test("2 annotations") {})

            // This would be nice, but listOf doesn't do the right thing
            // isNodeBuilder(listOf(UnitAnnotation, AnyAnnotation, YetAnotherAnnotation) - test("3 annotations") {})

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
            "  ✓ 3 annotations",
            "  ▾ annotate with",
            "    ✓ in annotate with")
        
    }


    private fun checkLog(tests: RootContextBuilder<*>, vararg expected: String) {
        executeTests(tests)
        assertLogged(log, *expected)
    }
}

object UnitAnnotation : TestAnnotation<Unit>
object AnyAnnotation : TestAnnotation<Any?>
object YetAnotherAnnotation : TestAnnotation<Unit>

// check that expression is a nodebuilder at compile time
private fun <F> isNodeBuilder(nodeBuilder: NodeBuilder<F>) = nodeBuilder

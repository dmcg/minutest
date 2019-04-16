@file:Suppress("unused", "UNUSED_VARIABLE")

package dev.minutest.experimental

import dev.minutest.rootContext
import org.junit.jupiter.api.Test as JUnitTest

// Nothing is actually run in this file - it's just to check what compiles

private fun `annotations can only be applied if their fixture type is a supertype of the node fixture type`() {

    rootContext<Number> {
        fixture { 42 }
        isNodeBuilder(NumberAnnotation - test("") {})
        isNodeBuilder(AnyAnnotation - test("") {})

        isNodeBuilder(AnyAnnotation + NumberAnnotation - test("") {})

        // Don't compile
        // IntAnnotation - test("") {}
        // UnitAnnotation - test("") {}
        // StringAnnotation - test("") {}
    }

    rootContext<Int> {
        fixture { 42 }
        isNodeBuilder(NumberAnnotation - test("") {})
        isNodeBuilder(AnyAnnotation - test("") {})
        isNodeBuilder(IntAnnotation - test("") {})

        isNodeBuilder(AnyAnnotation + NumberAnnotation - test("") {})
        isNodeBuilder(AnyAnnotation + NumberAnnotation + IntAnnotation - test("") {})

        // Don't compile
        // UnitAnnotation - test("") {}
        // StringAnnotation - test("") {}

        // Doesn't compile because of the Iterable<*>.minus overload
        // AnyAnnotation + NumberAnnotation + StringAnnotation - test("") {}
    }
}


private object UnitAnnotation : TestAnnotation<Unit>
private object AnyAnnotation : TestAnnotation<Any?>
private object NumberAnnotation : TestAnnotation<Number>
private object IntAnnotation : TestAnnotation<Int>
private object StringAnnotation : TestAnnotation<String>

private fun <F> HOLE(): F = throw NotImplementedError()
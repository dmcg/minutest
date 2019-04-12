@file:Suppress("unused", "UNUSED_VARIABLE")

package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeTransform
import dev.minutest.rootContext
import org.junit.jupiter.api.Test as JUnitTest

// Nothing is actually run in this file - it's just to check what compiles

private fun `annotations can only be applied if their fixture type is a supertype of the node fixture type`() {

    rootContext<Number> {
        fixture { 42 }
        isNodeBuilder(NumberAnnotation - test("") {})
//        isNodeBuilder(AnyAnnotation - test("") {})

//        isNodeBuilder(AnyAnnotation + NumberAnnotation - test("") {})

        // Don't compile
        // IntAnnotation - test("") {}
        // UnitAnnotation - test("") {}
        // StringAnnotation - test("") {}
    }

    rootContext<Int> {
        fixture { 42 }
//        isNodeBuilder(NumberAnnotation - test("") {})
//        isNodeBuilder(AnyAnnotation - test("") {})
        isNodeBuilder(IntAnnotation - test("") {})

//        isNodeBuilder(AnyAnnotation + NumberAnnotation - test("") {})
//        isNodeBuilder(AnyAnnotation + NumberAnnotation + IntAnnotation - test("") {})

        // Don't compile
        // UnitAnnotation - test("") {}
        // StringAnnotation - test("") {}

        // Doesn't compile because of the Iterable<*>.minus overload
        // AnyAnnotation + NumberAnnotation + StringAnnotation - test("") {}
    }
}


private fun `scope for looking at node variance`() {

    val numberNode: Node<Number> = HOLE()
    val intNode: Node<Int> = HOLE()
    val anyNode: Node<Any> = HOLE()

    // Node is <in F> - the fixture object is passed in by the parent
//    val intNode2: Node<Int> = numberNode
//        val numberNode2: Node<Number> = intNode

    // NodeTransform is a function, (<in Node<F>>) -> <out Node<F>>
    val numberTransform1: NodeTransform<Number> = { node: Node<Number> -> numberNode }
    // So we can narrow the input and widen the output
//    val numberTransform2: NodeTransform<Number> = { node: Node<Int> -> anyNode }
    // But not vv
    // val numberTransform3: NodeTransform<Number> = { node: Node<Number> -> intNode }

    // Annotations only accept transforms that are contravariant
    val numberAnnotation1: TransformingAnnotation<Number> = TransformingAnnotation(HOLE<NodeTransform<Number>>())
//    val numberAnnotation2: TransformingAnnotation<Number> = TransformingAnnotation(HOLE<NodeTransform<Any>>())
    // val numberAnnotation3: TransformingAnnotation<Number> = TransformingAnnotation(HOLE<NodeTransform<Int>>())

}

private object UnitAnnotation : TestAnnotation<Unit>
private object AnyAnnotation : TestAnnotation<Any?>
private object NumberAnnotation : TestAnnotation<Number>
private object IntAnnotation : TestAnnotation<Int>
private object StringAnnotation : TestAnnotation<String>

private fun <F> HOLE(): F = throw NotImplementedError()
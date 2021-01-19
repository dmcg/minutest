package dev.minutest.experimental

import dev.minutest.*

/**
 * A [TestAnnotation] that adds a transform to be applied to the [Node].
 */
abstract class TransformingAnnotation : TestAnnotation {

    override fun applyTo(annotatable: Annotatable<*>) {
        @Suppress("UNCHECKED_CAST") // I haven't (yet) found a way that this isn't safe
        annotatable.addTransform(this.asNodeTransform<Any?>() as (Node<out Any?>) -> Nothing)
    }

    abstract fun <F> transform(node: Node<F>): Node<F>

    private fun <F> asNodeTransform(): NodeTransform<F> = this::transform
}

private object ShouldCompile {
    // A TransformingAnnotation

    // * should be able to return the same node
    val id: TransformingAnnotation = object : TransformingAnnotation() {
        override fun <F> transform(node: Node<F>): Node<F> = node
    }

    // * should be able to access the fixture, provided it doesn't assume anything about its type
    val testLogging: TransformingAnnotation = object : TransformingAnnotation() {
        override fun <F> transform(node: Node<F>): Node<F> =
            when (node) {
                is Context<F, *> -> node
                is Test<F> -> Test(node.name, node.markers, node.id) { fixture, testDescriptor ->
                    println("Before ${testDescriptor.name}: $fixture")
                    node.invoke(fixture, testDescriptor).also {
                        println("After ${testDescriptor.name}: $it")
                    }
                }
            }
    }
}

//private object `Should not compile` {
//    // A TransformingAnnotation
//
//    // * should not be able to return a node that expects a particular fixture type to be supplied
//
//    val `transforms to a node that expects a particular fixture type` = object: TransformingAnnotation()
//    {
//        override fun <F> transform(node: Node<F>): Node<F> =
//            Test<F>("name", emptyList()) { fixture: Int, _ -> // compile failure - Expected parameter of type F
//                fixture // compile failure - Required: F, Found: Int
//        }
//    }
//
//    val `transforms to a particular node type` = object: TransformingAnnotation()
//    {
//        override fun <F> transform(node: Node<F>): Node<F> =
//            Test<Int>("name", emptyList()) { _ , _ -> // compile failure - Required: Node<F>, Found: Test<Int>
//        }
//    }
//
//}
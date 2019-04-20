package dev.minutest.experimental

import dev.minutest.*

/**
 * A [TestAnnotation] that adds a transform to be applied to the [Node].
 */
class TransformingAnnotation(
    private val transform: NodeTransform<Any?>
) : TestAnnotation {

    override fun applyTo(nodeBuilder: NodeBuilder<*>) {
        @Suppress("UNCHECKED_CAST") // I haven't (yet) found a way that this isn't safe
        nodeBuilder.addTransform(transform as (Node<out Any?>) -> Nothing)
    }
}

private object ShouldCompile {
    // A TransformingAnnotation

    // * should be fine returning the same node
    val id: TransformingAnnotation = TransformingAnnotation { node -> node }

    // * should be able to access the fixture, provided it doesn't assume anything about its type
    val testLogging: TransformingAnnotation = TransformingAnnotation { node ->
        when (node) {
            is Context<*, *> -> node
            is Test<Any?> -> Test(node.name, node.markers) { fixture, testDescriptor ->
                println("Before ${testDescriptor.name}: $fixture")
                node.invoke(fixture, testDescriptor).also {
                    println("After ${testDescriptor.name}: $it")
                }
            }
        }
    }
}

//@Suppress("unused")
//private object `Should not compile` {
//    // A TransformingAnnotation
//
//    // * should not be able to return a node that expects a particular fixture type to be supplied
//
//    val `transforms to node that expects a particular fixture type` = TransformingAnnotation { node ->
//        Test<Int>("name", emptyList()) { anInt, _ -> // compile failure - Required: Node<Any?>, Found: Test<Int>
//            anInt + 2
//        }
//    }
//
//    // * should not be able to assert that the node requires a particular fixture type
//
//    val `transforms expects a particular fixture type` = TransformingAnnotation {
//        node: Node<Int> -> node // compile failure - Expected parameter of type Node<Any?>
//    }
//
//    // * should not be able to accidentally pass a typed transform to TransformingAnnotation
//
//    val intTransform: NodeTransform<Int> = { node -> node }
//    val `can't pass transform with type to TransformingAnnotation` =
//        TransformingAnnotation(intTransform) // compile failure - Required: NodeTransform<Any?>, Found: NodeTransform<Int>
//
//    @Suppress("UNCHECKED_CAST")
//    val `could cast, but on your head be it`  = TransformingAnnotation(intTransform as NodeTransform<Any?>)
//
//    val `can't do an unsafe thing by assignment`: NodeTransform<Any?> =
//        intTransform // compile failure - Required: NodeTransform<Any?>, Found: NodeTransform<Int>
//
//    @Suppress("UNCHECKED_CAST")
//    val `can cast, but but on your head be it`: NodeTransform<Any?> = intTransform as NodeTransform<Any?>
//}



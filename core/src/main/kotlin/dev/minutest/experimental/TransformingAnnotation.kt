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

//private object `Should not compile` {
//    // A TransformingAnnotation
//
//    // * should not be able to return a node that expects a particular fixture type to be supplied
//    val `transforms to node that expects a particular fixture type` = TransformingAnnotation { node ->
//        Test<Int>("name", emptyList()) { anInt, _ ->
//            anInt + 2
//        }
//    }
//
//    // * should not be able to assert that the node requires a particular fixture type
//    val `transforms expects a particular fixture type` = TransformingAnnotation { node: Node<Int> -> node }
//}



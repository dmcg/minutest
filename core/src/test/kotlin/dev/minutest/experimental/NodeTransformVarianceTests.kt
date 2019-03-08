package dev.minutest.experimental

import dev.minutest.Node


private fun scope() {
    val intNode = HOLE<Node<Int>>()
    val numberNode = HOLE<Node<Number>>()

    // No variance
    val intNodeTransform = HOLE<NewNodeTransform<Int>>()
    val newIntNode: Node<Int> = intNodeTransform.transform(intNode)
    val numberNodeTransform = HOLE<NewNodeTransform<Number>>()
    val newNumberNode: Node<Number> = numberNodeTransform.transform(numberNode)

    // NodeTransform<Number> can act on Node<Int>, as the fixture will be Int, which is in compatible with Number
    val newIntNode2: Node<Int> = numberNodeTransform.transform(intNode)

    // but not vv
    val fred: Node<Int> = intNodeTransform.transform(numberNode)

}

interface NewNodeTransform<in F> {
    fun <F2: F> transform(node: Node<F2>): Node<F2>
}

//object IntNodeTransform : NewNodeTransform<Int> {
//    override fun <F2 : Int> transform(node: Node<F2>): Node<F2> {
//        return HOLE<Node<Number>>()
//    }
//}

fun <F> HOLE(): F = throw NotImplementedError()

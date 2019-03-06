package dev.minutest

interface NodeTransform {

    fun <F> transform(node: Node<F>): Node<F>

    fun then(next: (NodeTransform)): NodeTransform = object: NodeTransform {
        override fun <F> transform(node: Node<F>): Node<F> =
            next.transform(this@NodeTransform.transform(node))
    }
}

internal fun <F> NodeTransform.transform(nodes: Iterable<Node<F>>) = nodes.map { transform(it) }

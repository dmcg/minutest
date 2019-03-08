package dev.minutest

interface NodeTransform<F> {

    fun transform(node: Node<F>): Node<F>

    fun then(next: (NodeTransform<F>)): NodeTransform<F> = object: NodeTransform<F> {
        override fun transform(node: Node<F>): Node<F> =
            next.transform(this@NodeTransform.transform(node))
    }
}

fun <F> NodeTransform(f: (Node<F>) -> Node<F>): NodeTransform<F> = object: NodeTransform<F> {
    override fun transform(node: Node<F>): Node<F> = f(node)
}

internal fun <F> NodeTransform<in F>.transformAll(nodes: Iterable<Node<F>>) = nodes.map { transform(it as Node<Any?>) }

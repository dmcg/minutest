package dev.minutest

interface NodeTransform<F> {

    fun transform(node: Node<F>): Node<F>

    fun then(next: (NodeTransform<F>)): NodeTransform<F> = create { node ->
        next.transform(this@NodeTransform.transform(node))
    }

    companion object {
        fun <F> create(f: (Node<F>) -> Node<F>): NodeTransform<F> = object: NodeTransform<F> {
            override fun transform(node: Node<F>): Node<F> = f(node)
        }
    }
}


internal fun <F> NodeTransform<in F>.transformAll(nodes: Iterable<Node<F>>) = nodes.map { node ->
    @Suppress("UNCHECKED_CAST") // TODO - I haven't really worked out what this cast is telling me
    transform(node as Node<Any?>)
}

package dev.minutest

typealias NodeTransform<F> = (Node<F>) -> Node<F>

typealias RootTransform = NodeTransform<Unit>

fun <F> NodeTransform<F>.then(next: NodeTransform<F>): NodeTransform<F> = { node ->
    next(this(node))
}

internal fun <F> NodeTransform<F>.transformAll(nodes: Iterable<Node<F>>) = nodes.map { node ->
    invoke(node)
}

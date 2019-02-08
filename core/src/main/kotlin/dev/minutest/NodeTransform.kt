package dev.minutest

interface NodeTransform {

    fun <F> applyTo(node: dev.minutest.Node<F>): dev.minutest.Node<F>

    fun then(next: (NodeTransform)): NodeTransform = object: NodeTransform {
        override fun <F> applyTo(node: dev.minutest.Node<F>): dev.minutest.Node<F> =
            next.applyTo(this@NodeTransform.applyTo(node))
    }
}

internal fun <F> NodeTransform.applyTo(nodes: Iterable<dev.minutest.Node<F>>) = nodes.map { applyTo(it) }

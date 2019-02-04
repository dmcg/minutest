package uk.org.minutest

interface NodeTransform {

    fun <F> applyTo(node: uk.org.minutest.Node<F>): uk.org.minutest.Node<F>

    fun then(next: (NodeTransform)): NodeTransform = object: NodeTransform {
        override fun <F> applyTo(node: uk.org.minutest.Node<F>): uk.org.minutest.Node<F> =
            next.applyTo(this@NodeTransform.applyTo(node))
    }
}

internal fun <F> NodeTransform.applyTo(nodes: Iterable<uk.org.minutest.Node<F>>) = nodes.map { applyTo(it) }

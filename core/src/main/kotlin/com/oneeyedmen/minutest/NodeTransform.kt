package com.oneeyedmen.minutest

interface NodeTransform {

    fun <F> applyTo(node: Node<F>): Node<F>

    fun then(next: (NodeTransform)): NodeTransform = object: NodeTransform {
        override fun <F> applyTo(node: Node<F>): Node<F> =
            next.applyTo(this@NodeTransform.applyTo(node))
    }
}
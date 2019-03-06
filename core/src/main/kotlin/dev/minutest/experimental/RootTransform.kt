package dev.minutest.experimental

import dev.minutest.Node


interface RootTransform {

    fun transformRoot(node: Node<Unit>): Node<Unit>

    fun then(next: (RootTransform)): RootTransform = object: RootTransform {
        override fun transformRoot(node: Node<Unit>): Node<Unit> =
            next.transformRoot(this@RootTransform.transformRoot(node))
    }
}
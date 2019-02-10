package dev.minutest.experimental

import dev.minutest.Node


interface TopLevelTransform {

    fun applyTo(node: Node<Unit>): Node<Unit>

    fun then(next: (TopLevelTransform)): TopLevelTransform = object: TopLevelTransform {
        override fun applyTo(node: Node<Unit>): Node<Unit> =
            next.applyTo(this@TopLevelTransform.applyTo(node))
    }
}
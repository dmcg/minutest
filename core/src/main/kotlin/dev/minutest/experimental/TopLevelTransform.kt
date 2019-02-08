package dev.minutest.experimental


interface TopLevelTransform {

    fun applyTo(node: dev.minutest.Node<Unit>): dev.minutest.Node<Unit>

    fun then(next: (TopLevelTransform)): TopLevelTransform = object: TopLevelTransform {
        override fun applyTo(node: dev.minutest.Node<Unit>): dev.minutest.Node<Unit> =
            next.applyTo(this@TopLevelTransform.applyTo(node))
    }
}
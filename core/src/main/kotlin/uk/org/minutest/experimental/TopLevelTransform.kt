package uk.org.minutest.experimental


interface TopLevelTransform {

    fun applyTo(node: uk.org.minutest.Node<Unit>): uk.org.minutest.Node<Unit>

    fun then(next: (TopLevelTransform)): TopLevelTransform = object: TopLevelTransform {
        override fun applyTo(node: uk.org.minutest.Node<Unit>): uk.org.minutest.Node<Unit> =
            next.applyTo(this@TopLevelTransform.applyTo(node))
    }
}
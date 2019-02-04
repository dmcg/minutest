package uk.org.minutest.experimental

interface TestAnnotation {

    fun applyTo(nodeBuilder: uk.org.minutest.NodeBuilder<*>) {
        nodeBuilder.annotations.add(this)
    }

    fun appliesTo(node: uk.org.minutest.Node<*>) = node.annotations.contains(this)

    operator fun plus(that: TestAnnotation) = listOf(this, that)

    operator fun <F, NodeBuilderT: uk.org.minutest.NodeBuilder<F>> minus(nodeBuilder: NodeBuilderT): NodeBuilderT =
        nodeBuilder.also {
            this.applyTo(it)
        }
}

operator fun <F, NodeBuilderT: uk.org.minutest.NodeBuilder<F>> Iterable<TestAnnotation>.minus(nodeBuilder: NodeBuilderT): NodeBuilderT=
    nodeBuilder.also {
        this.forEach { annotation ->
            annotation.applyTo(nodeBuilder)
        }
    }

fun uk.org.minutest.TestContextBuilder<*, *>.annotateWith(annotation: TestAnnotation) {
    annotation.applyTo(this as uk.org.minutest.NodeBuilder<*>)
}
package dev.minutest.experimental

interface TestAnnotation {

    fun applyTo(nodeBuilder: dev.minutest.NodeBuilder<*>) {
        nodeBuilder.annotations.add(this)
    }

    fun appliesTo(node: dev.minutest.Node<*>) = node.annotations.contains(this)

    operator fun plus(that: TestAnnotation) = listOf(this, that)

    operator fun <F, NodeBuilderT: dev.minutest.NodeBuilder<F>> minus(nodeBuilder: NodeBuilderT): NodeBuilderT =
        nodeBuilder.also {
            this.applyTo(it)
        }
}

operator fun <F, NodeBuilderT: dev.minutest.NodeBuilder<F>> Iterable<TestAnnotation>.minus(nodeBuilder: NodeBuilderT): NodeBuilderT=
    nodeBuilder.also {
        this.forEach { annotation ->
            annotation.applyTo(nodeBuilder)
        }
    }

fun dev.minutest.TestContextBuilder<*, *>.annotateWith(annotation: TestAnnotation) {
    annotation.applyTo(this as dev.minutest.NodeBuilder<*>)
}
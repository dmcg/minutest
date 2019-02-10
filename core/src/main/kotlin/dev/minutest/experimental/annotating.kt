package dev.minutest.experimental

import dev.minutest.Node
import dev.minutest.NodeBuilder
import dev.minutest.TestContextBuilder

interface TestAnnotation {

    fun applyTo(nodeBuilder: NodeBuilder<*>) {
        nodeBuilder.annotations.add(this)
    }

    fun appliesTo(node: Node<*>) = node.annotations.contains(this)

    operator fun plus(that: TestAnnotation) = listOf(this, that)

    operator fun <F, NodeBuilderT: NodeBuilder<F>> minus(nodeBuilder: NodeBuilderT): NodeBuilderT =
        nodeBuilder.also {
            this.applyTo(it)
        }
}

operator fun <F, NodeBuilderT: NodeBuilder<F>> Iterable<TestAnnotation>.minus(nodeBuilder: NodeBuilderT): NodeBuilderT=
    nodeBuilder.also {
        this.forEach { annotation ->
            annotation.applyTo(nodeBuilder)
        }
    }

fun TestContextBuilder<*, *>.annotateWith(annotation: TestAnnotation) {
    annotation.applyTo(this as NodeBuilder<*>)
}
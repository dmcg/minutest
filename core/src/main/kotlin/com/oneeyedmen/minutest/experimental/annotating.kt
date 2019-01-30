package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.GeneralContextBuilder
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeNode

interface TestAnnotation {

    fun applyTo(nodeBuilder: NodeBuilder<*>) {
        addTo(nodeBuilder.annotations)
    }

    fun addTo(properties: MutableList<TestAnnotation>) {
        properties.add(this)
    }

    fun appliesTo(runtimeNode: RuntimeNode<*>) = runtimeNode.annotations.contains(this)

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

fun GeneralContextBuilder<*, *>.annotateWith(annotation: TestAnnotation) {
    annotation.applyTo(this as NodeBuilder<*>)
}
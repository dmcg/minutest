package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeContext
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

fun Context<*, *>.annotateWith(annotation: TestAnnotation) {
    annotation.applyTo(this as NodeBuilder<*>)
}

fun <PF, F> RuntimeContext<PF, F>.withTransformedChildren(transform: (RuntimeNode<F>) -> RuntimeNode<F>) =
    withChildren(children.map(transform))
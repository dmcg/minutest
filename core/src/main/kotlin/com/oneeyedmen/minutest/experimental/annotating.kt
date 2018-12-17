package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode

interface TestAnnotation {

    fun applyTo(nodeBuilder: NodeBuilder<*, *>) {
        addTo(nodeBuilder.properties)
    }

    fun appliesTo(runtimeNode: RuntimeNode<*, *>) = runtimeNode.properties.containsKey(this)

    fun addTo(properties: MutableMap<Any, Any>) {
        properties[this] = true
    }

    operator fun plus(that: TestAnnotation) = listOf(this, that)

    operator fun <PF, F, NodeBuilderT: NodeBuilder<PF, F>> minus(nodeBuilder: NodeBuilderT): NodeBuilderT =
        nodeBuilder.also {
            this.applyTo(it)
        }
}

operator fun <PF, F, NodeBuilderT: NodeBuilder<PF, F>> Iterable<TestAnnotation>.minus(nodeBuilder: NodeBuilderT): NodeBuilderT=
    nodeBuilder.also {
        this.forEach { annotation ->
            annotation.applyTo(nodeBuilder)
        }
    }

fun Context<*, *>.annotateWith(annotation: TestAnnotation) {
    annotation.applyTo(this as NodeBuilder<*, *>)
}

fun <F> ((RuntimeContext<Unit, F>) -> RuntimeContext<Unit, F>).then(
    next: (RuntimeContext<Unit, F>) -> RuntimeContext<Unit, F>
) = { context: RuntimeContext<Unit, F> ->
    next(this(context))
}

fun <PF, F> RuntimeContext<PF, F>.withTransformedChildren(transform: (RuntimeNode<F, *>) -> RuntimeNode<F, *>) =
    adopting(children.map(transform))
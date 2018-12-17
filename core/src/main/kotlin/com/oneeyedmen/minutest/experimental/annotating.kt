package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode

interface TestAnnotation {

    fun applyTo(nodeBuilder: NodeBuilder<*, *>) {
        addTo(nodeBuilder.properties)
    }

    fun appliesTo(runtimeNode: RuntimeNode) = runtimeNode.properties.containsKey(this)

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

fun <F> ((RuntimeContext<F>) -> RuntimeContext<F>).then(next: (RuntimeContext<F>) -> RuntimeContext<F>) = { context: RuntimeContext<F> ->
    next(this(context))
}

fun <F> RuntimeContext<F>.withTransformedChildren(transform: (RuntimeNode) -> RuntimeNode) =
    adopting(children.map(transform))
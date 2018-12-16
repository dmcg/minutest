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

    operator fun <PF, F> minus(nodeBuilder: NodeBuilder<PF, F>): NodeBuilder<PF, F> =
        nodeBuilder.also {
            this.applyTo(it)
        }
}

operator fun <PF, F> Iterable<TestAnnotation>.minus(nodeBuilder: NodeBuilder<PF, F>): NodeBuilder<PF, F> =
    nodeBuilder.also {
        this.forEach { annotation ->
            annotation.applyTo(nodeBuilder)
        }
    }

fun Context<*, *>.annotateWith(annotation: TestAnnotation) {
    annotation.applyTo(this as NodeBuilder<*, *>)
}

fun ((RuntimeNode) -> RuntimeNode).then(next: (RuntimeNode) -> RuntimeNode) = { node: RuntimeNode ->
    next(this(node))
}

fun <F> RuntimeContext<F>.withTransformedChildren(transform: (RuntimeNode) -> RuntimeNode) =
    adopting(children.map(transform))
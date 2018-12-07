package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode

interface TestAnnotation {
    fun applyTo(nodeBuilder: NodeBuilder<*>) {
        addTo(nodeBuilder.properties)
    }

    fun appliesTo(runtimeNode: RuntimeNode) = runtimeNode.properties.containsKey(this)

    fun addTo(properties: MutableMap<Any, Any>) {
        properties[this] = true
    }
}

operator fun <F> TestAnnotation.minus(nodeBuilder: NodeBuilder<F>): NodeBuilder<F> =
    nodeBuilder.also {
        this.applyTo(it)
    }


fun Context<*, *>.annotateWith(annotation: TestAnnotation) {
    annotation.applyTo(this as NodeBuilder<*>)
}

fun ((RuntimeNode) -> RuntimeNode).then(next: (RuntimeNode) -> RuntimeNode) = { node: RuntimeNode ->
    next(this(node))
}

fun RuntimeContext.withTransformedChildren(transform: (RuntimeNode) -> RuntimeNode) =
    withChildren(children.map(transform))
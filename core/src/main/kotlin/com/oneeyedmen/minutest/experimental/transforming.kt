package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.Node
import com.oneeyedmen.minutest.NodeTransform
import com.oneeyedmen.minutest.Test


fun <F> Node<F>.transformedBy(annotations: List<TestAnnotation>): Node<F> {
    val transforms: List<NodeTransform> = annotations.filterIsInstance<NodeTransform>()
    return if (transforms.isEmpty())
        this
    else {
        transforms.reduce(NodeTransform::then).applyTo(this)
    }
}

fun Node<*>.hasA(predicate: (Node<*>) -> Boolean): Boolean = when (this) {
    is Test<*> -> predicate(this)
    is Context<*, *> -> hasA(predicate)
}

fun Context<*, *>.hasA(predicate: (Node<*>) -> Boolean): Boolean {
    return predicate(this) || children.find { it.hasA(predicate) } != null
}
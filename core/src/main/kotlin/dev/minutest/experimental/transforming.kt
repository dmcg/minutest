package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.NodeTransform
import dev.minutest.Test


fun <F> Node<F>.transformedBy(annotations: List<TestAnnotation<F>>): Node<F> =
    if (annotations.isEmpty())
        this
    else
        annotations
            .map { it.transformOfType<F>() }
            .reduce(NodeTransform<F>::then)
            .transform(this)

fun Node<*>.hasA(predicate: (Node<*>) -> Boolean): Boolean = when (this) {
    is Test<*> -> predicate(this)
    is Context<*, *> -> hasA(predicate)
}

fun Context<*, *>.hasA(predicate: (Node<*>) -> Boolean): Boolean {
    return predicate(this) || children.find { it.hasA(predicate) } != null
}
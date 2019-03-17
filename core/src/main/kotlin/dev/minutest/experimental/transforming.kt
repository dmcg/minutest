package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.NodeTransform
import dev.minutest.Test


fun <F> Node<F>.transformedBy(annotations: Iterable<TestAnnotation<F>>): Node<F> =
    this.transformedBy(annotations.mapNotNull { it.transformOfType<F>() })

fun <F> Node<F>.transformedBy(transforms: Collection<NodeTransform<F>>): Node<F> =
    if (transforms.isEmpty())
        this
    else
        transforms.reversed().reduce(NodeTransform<F>::then).transform(this)

fun Node<*>.hasA(predicate: (Node<*>) -> Boolean): Boolean = when (this) {
    is Test<*> -> predicate(this)
    is Context<*, *> -> hasA(predicate)
}

fun Context<*, *>.hasA(predicate: (Node<*>) -> Boolean): Boolean =
    predicate(this) || children.find { it.hasA(predicate) } != null
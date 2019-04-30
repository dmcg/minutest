package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.NodeTransform
import dev.minutest.Test

internal fun <F> Node<F>.transformedBy(transforms: Collection<NodeTransform<F>>): Node<F> =
    if (transforms.isEmpty())
        this
    else
        transforms.reversed().reduce(NodeTransform<F>::then)(this)

internal fun <F> NodeTransform<F>.then(next: NodeTransform<F>): NodeTransform<F> = { node ->
    next(this(node))
}

fun Node<*>.hasA(predicate: (Node<*>) -> Boolean): Boolean = when (this) {
    is Test<*> -> predicate(this)
    is Context<*, *> -> hasA(predicate)
}

fun Context<*, *>.hasA(predicate: (Node<*>) -> Boolean): Boolean =
    predicate(this) || children.find { it.hasA(predicate) } != null
package dev.minutest.experimental

import dev.minutest.*

fun <F> Node<F>.transformedBy(transforms: Collection<NodeTransform<F>>): Node<F> =
    if (transforms.isEmpty())
        this
    else
        transforms.reduce(NodeTransform<F>::then)(this)

fun Node<*>.hasA(predicate: (Node<*>) -> Boolean): Boolean = when (this) {
    is Test<*> -> predicate(this)
    is Context<*, *> -> hasA(predicate)
}

fun Context<*, *>.hasA(predicate: (Node<*>) -> Boolean): Boolean =
    predicate(this) || children.find { it.hasA(predicate) } != null
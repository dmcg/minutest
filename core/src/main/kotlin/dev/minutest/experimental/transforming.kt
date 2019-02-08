package dev.minutest.experimental

import dev.minutest.NodeTransform


fun <F> dev.minutest.Node<F>.transformedBy(annotations: List<TestAnnotation>): dev.minutest.Node<F> {
    val transforms: List<NodeTransform> = annotations.filterIsInstance<NodeTransform>()
    return if (transforms.isEmpty())
        this
    else {
        transforms.reduce(NodeTransform::then).applyTo(this)
    }
}

fun dev.minutest.Node<*>.hasA(predicate: (dev.minutest.Node<*>) -> Boolean): Boolean = when (this) {
    is dev.minutest.Test<*> -> predicate(this)
    is dev.minutest.Context<*, *> -> hasA(predicate)
}

fun dev.minutest.Context<*, *>.hasA(predicate: (dev.minutest.Node<*>) -> Boolean): Boolean {
    return predicate(this) || children.find { it.hasA(predicate) } != null
}
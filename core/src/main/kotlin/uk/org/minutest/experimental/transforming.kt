package uk.org.minutest.experimental

import uk.org.minutest.NodeTransform


fun <F> uk.org.minutest.Node<F>.transformedBy(annotations: List<TestAnnotation>): uk.org.minutest.Node<F> {
    val transforms: List<NodeTransform> = annotations.filterIsInstance<NodeTransform>()
    return if (transforms.isEmpty())
        this
    else {
        transforms.reduce(NodeTransform::then).applyTo(this)
    }
}

fun uk.org.minutest.Node<*>.hasA(predicate: (uk.org.minutest.Node<*>) -> Boolean): Boolean = when (this) {
    is uk.org.minutest.Test<*> -> predicate(this)
    is uk.org.minutest.Context<*, *> -> hasA(predicate)
}

fun uk.org.minutest.Context<*, *>.hasA(predicate: (uk.org.minutest.Node<*>) -> Boolean): Boolean {
    return predicate(this) || children.find { it.hasA(predicate) } != null
}
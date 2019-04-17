package dev.minutest

typealias NodeTransform<F> = (Node<F>) -> Node<F>

/**
 * A transform to be applied to the root [Node].
 */
typealias RootTransform = NodeTransform<Unit>


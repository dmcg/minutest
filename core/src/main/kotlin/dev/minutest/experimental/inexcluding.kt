package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test

/**
 * [TestAnnotation] that will cause a [Test] or [Context] to be skipped.
 */
val SKIP = TransformingAnnotation { it.skipped() }

/**
 * [TestAnnotation] that will cause a [Test] or [Context] to be run while those not marked [FOCUS] will
 * be skipped.
 */
val FOCUS = RootAnnotation { node ->
    when (node) {
        is Context<Unit, *> -> node.inexcluded(node.hasAFocusedChild())
        is Test<Unit> -> TODO("skipAndFocus when root is a test")
    }
}

private fun <F> inexclude(skipIsDefault: Boolean): (Node<F>) -> Node<F> = { node ->
    when (node) {
        is Context<F, *> -> node.inexcluded(skipIsDefault)
        is Test<F> -> node.inexcluded(skipIsDefault)
    }
}

private fun <PF, F> Context<PF, F>.inexcluded(skipIsDefault: Boolean): Node<PF> = when {
    FOCUS.appliesTo(this) -> this.withTransformedChildren(inexclude(skipIsDefault = false))
    this.hasAFocusedChild() -> this.withTransformedChildren(inexclude(skipIsDefault))
    skipIsDefault -> this.skipped()
    else -> this.withTransformedChildren(inexclude(skipIsDefault))
}

private fun <F> Test<F>.inexcluded(skipIsDefault: Boolean): Test<F> = when {
    FOCUS.appliesTo(this) -> this
    skipIsDefault -> this.skipped()
    else -> this
}

private fun Context<*, *>.hasAFocusedChild(): Boolean = this.hasA(FOCUS::appliesTo)

private fun <F> Node<F>.skipped() = Test<F>(name, annotations) { _, _ -> throw MinutestSkippedException() }

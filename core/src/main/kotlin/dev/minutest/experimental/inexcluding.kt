package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.NodeBuilder
import dev.minutest.Test

object SkipBuilder{
    operator fun <F, NodeBuilderT: NodeBuilder<F>> minus(nodeBuilder: NodeBuilderT): NodeBuilderT =
        TransformingAnnotation<F> { it.skipped() } - nodeBuilder
}

/**
* Builder to create a [TestAnnotation] that will cause a [Test] or [Context] to be skipped.
*/
val SKIP = SkipBuilder

object FocusBuilder {
    operator fun <F, NodeBuilderT: NodeBuilder<F>> minus(nodeBuilder: NodeBuilderT): NodeBuilderT {
        @Suppress("UNCHECKED_CAST" /* safe because we know focus does not do anything with the fixture */)
        val typedFocus = focus as TestAnnotation<F>
        return typedFocus - nodeBuilder
    }
}
/**
 * We have a single focus for efficiency reasons
 * We could also create a subclass (e.g. FocusedTestAnnotation) and instantiate it each time when one uses `FOCUS - ...`
 */
private val focus = RootAnnotation<Nothing> { node ->
    when (node) {
        is Context<Unit, *> -> node.inexcluded(node.hasAFocusedChild())
        is Test<Unit> -> TODO("skipAndFocus when root is a test")
    }
}

/**
 * Builder to create a [TestAnnotation] that will cause a [Test] or [Context] to be run while those not marked [FOCUS] will
 * be skipped.
 */
val FOCUS = FocusBuilder

private fun <F> inexclude(skipIsDefault: Boolean): (Node<F>) -> Node<F> = { node ->
    when (node) {
        is Context<F, *> -> node.inexcluded(skipIsDefault)
        is Test<F> -> node.inexcluded(skipIsDefault)
    }
}

private fun <PF, F> Context<PF, F>.inexcluded(skipIsDefault: Boolean): Node<PF> = when {
    focus.appliesTo(this) -> this.withTransformedChildren(inexclude(skipIsDefault = false))
    this.hasAFocusedChild() -> this.withTransformedChildren(inexclude(skipIsDefault))
    skipIsDefault -> this.skipped()
    else -> this.withTransformedChildren(inexclude(skipIsDefault))
}

private fun <F> Test<F>.inexcluded(skipIsDefault: Boolean): Test<F> = when {
    focus.appliesTo(this) -> this
    skipIsDefault -> this.skipped()
    else -> this
}

private fun Context<*, *>.hasAFocusedChild(): Boolean = this.hasA(focus::appliesTo)

private fun <F> Node<F>.skipped() = Test<F>(name, annotations) { _, _ -> throw MinutestSkippedException() }

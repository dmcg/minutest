package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test


object SKIP : TransformingAnnotation<Any?>({ it.skipped() })

object FOCUS : RootAnnotation<Any?>({ node ->
    when (node) {
        is Context<Unit, *> ->
            node.withTransformedChildren(Inexcluded(defaultToSkip = node.hasAFocusedChild()))
        is Test<Unit> ->
            TODO("skipAndFocus when root is a test")
    }
})

private class Inexcluded(val defaultToSkip: Boolean) : (Node<*>) -> Node<Any?> {
    override fun invoke(node: Node<*>): Node<Any?> =
        when (node) {
            is Context<*, *> -> applyToContext(node)
            is Test<*> -> applyToTest(node)
        } as Node<Any?> // TODO - this cast

    private fun applyToContext(node: Context<*, *>): Node<Nothing> =
        when {
            FOCUS.appliesTo(node) ->
                node.withTransformedChildren(Inexcluded(defaultToSkip = false))
            node.hasAFocusedChild() ->
                node.withTransformedChildren(this)
            defaultToSkip -> node.skipped()
            else ->
                node.withTransformedChildren(this)
        }

    private fun applyToTest(node: Test<*>): Node<Nothing> =
        when {
            FOCUS.appliesTo(node) -> node
            defaultToSkip -> node.skipped()
            else -> node
        }
}

private fun Context<*, *>.hasAFocusedChild() = this.hasA(FOCUS::appliesTo)

private fun <F> Node<F>.skipped() = Test<F>(name, annotations) { _, _ -> throw MinutestSkippedException() }

package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.NodeTransform
import dev.minutest.Test


object SKIP : TransformingAnnotation<Any?>( { it.skipped() } )

object FOCUS : TestAnnotation<Any?>, RootTransform {
    override fun transformRoot(node: Node<Unit>): Node<Unit> = when (node) {
        is Context<Unit, *> ->
            node.withTransformedChildren(Inexcluded(defaultToSkip = node.hasAFocusedChild()))
        is Test<Unit> ->
            TODO("skipAndFocus on root as test")
    }
}

private class Inexcluded(val defaultToSkip: Boolean) : NodeTransform<Any?> {
    override fun transform(node: Node<Any?>): Node<Any?> =
        when (node) {
            is Context<Any?, *> -> applyToContext(node)
            is Test<Any?> -> applyToTest(node)
        }
    
    private fun applyToContext(node: Context<Any?, *>): Node<Any?> =
        when {
            FOCUS.appliesTo(node) ->
                node.withTransformedChildren(Inexcluded(defaultToSkip = false))
            node.hasAFocusedChild() ->
                node.withTransformedChildren(this)
            defaultToSkip -> node.skipped()
            else ->
                node.withTransformedChildren(this)
        }
    
    private fun <F> applyToTest(node: Test<F>): Test<F> =
        when {
            FOCUS.appliesTo(node) -> node
            defaultToSkip -> node.skipped()
            else -> node
        }
}

private fun Context<*, *>.hasAFocusedChild() =
    this.hasA(FOCUS::appliesTo)

private fun <F> Node<F>.skipped() =
    Test<F>(name, annotations) { _, _ -> throw MinutestSkippedException() }

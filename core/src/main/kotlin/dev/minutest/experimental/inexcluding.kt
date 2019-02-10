package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.NodeTransform
import dev.minutest.Test


object SKIP : TestAnnotation, NodeTransform {
    override fun <F> applyTo(node: Node<F>): Node<F> = node.skipped()
}

object FOCUS : TestAnnotation, TopLevelTransform {
    override fun applyTo(node: Node<Unit>): Node<Unit> = when (node) {
        is Context<Unit, *> ->
            node.withTransformedChildren(Inexcluded(defaultToSkip = node.hasAFocusedChild()))
        is Test<Unit> ->
            TODO("skipAndFocus on root as test")
    }
}

private class Inexcluded(val defaultToSkip: Boolean) : NodeTransform {
    override fun <F> applyTo(node: Node<F>): Node<F> =
        when (node) {
            is Context<F, *> -> applyToContext(node)
            is Test<F> -> applyToTest(node)
        }
    
    private fun <PF, F> applyToContext(node: Context<PF, F>): Node<PF> =
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

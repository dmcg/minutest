package dev.minutest.experimental

import dev.minutest.NodeTransform


object SKIP : TestAnnotation, NodeTransform {
    override fun <F> applyTo(node: dev.minutest.Node<F>): dev.minutest.Node<F> = node.skipped()
}

object FOCUS : TestAnnotation, TopLevelTransform {
    override fun applyTo(node: dev.minutest.Node<Unit>): dev.minutest.Node<Unit> = when (node) {
        is dev.minutest.Context<Unit, *> ->
            node.withTransformedChildren(Inexcluded(defaultToSkip = node.hasAFocusedChild()))
        is dev.minutest.Test<Unit> ->
            TODO("skipAndFocus on root as test")
    }
}

private class Inexcluded(val defaultToSkip: Boolean) : NodeTransform {
    override fun <F> applyTo(node: dev.minutest.Node<F>): dev.minutest.Node<F> =
        when (node) {
            is dev.minutest.Context<F, *> -> applyToContext(node)
            is dev.minutest.Test<F> -> applyToTest(node)
        }
    
    private fun <PF, F> applyToContext(node: dev.minutest.Context<PF, F>): dev.minutest.Node<PF> =
        when {
            FOCUS.appliesTo(node) ->
                node.withTransformedChildren(Inexcluded(defaultToSkip = false))
            node.hasAFocusedChild() ->
                node.withTransformedChildren(this)
            defaultToSkip -> node.skipped()
            else ->
                node.withTransformedChildren(this)
        }
    
    private fun <F> applyToTest(node: dev.minutest.Test<F>): dev.minutest.Test<F> =
        when {
            FOCUS.appliesTo(node) -> node
            defaultToSkip -> node.skipped()
            else -> node
        }
}

private fun dev.minutest.Context<*, *>.hasAFocusedChild() =
    this.hasA(FOCUS::appliesTo)

private fun <F> dev.minutest.Node<F>.skipped() =
    dev.minutest.Test<F>(name, annotations) { _, _ -> throw MinutestSkippedException() }

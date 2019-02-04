package uk.org.minutest.experimental

import uk.org.minutest.NodeTransform


object SKIP : TestAnnotation, NodeTransform {
    override fun <F> applyTo(node: uk.org.minutest.Node<F>): uk.org.minutest.Node<F> = node.skipped()
}

object FOCUS : TestAnnotation, TopLevelTransform {
    override fun applyTo(node: uk.org.minutest.Node<Unit>): uk.org.minutest.Node<Unit> = when (node) {
        is uk.org.minutest.Context<Unit, *> ->
            node.withTransformedChildren(Inexcluded(defaultToSkip = node.hasAFocusedChild()))
        is uk.org.minutest.Test<Unit> ->
            TODO("skipAndFocus on root as test")
    }
}

private class Inexcluded(val defaultToSkip: Boolean) : NodeTransform {
    override fun <F> applyTo(node: uk.org.minutest.Node<F>): uk.org.minutest.Node<F> =
        when (node) {
            is uk.org.minutest.Context<F, *> -> applyToContext(node)
            is uk.org.minutest.Test<F> -> applyToTest(node)
        }
    
    private fun <PF, F> applyToContext(node: uk.org.minutest.Context<PF, F>): uk.org.minutest.Node<PF> =
        when {
            FOCUS.appliesTo(node) ->
                node.withTransformedChildren(Inexcluded(defaultToSkip = false))
            node.hasAFocusedChild() ->
                node.withTransformedChildren(this)
            defaultToSkip -> node.skipped()
            else ->
                node.withTransformedChildren(this)
        }
    
    private fun <F> applyToTest(node: uk.org.minutest.Test<F>): uk.org.minutest.Test<F> =
        when {
            FOCUS.appliesTo(node) -> node
            defaultToSkip -> node.skipped()
            else -> node
        }
}

private fun uk.org.minutest.Context<*, *>.hasAFocusedChild() =
    this.hasA(FOCUS::appliesTo)

private fun <F> uk.org.minutest.Node<F>.skipped() =
    uk.org.minutest.Test<F>(name, annotations) { _, _ -> throw MinutestSkippedException() }

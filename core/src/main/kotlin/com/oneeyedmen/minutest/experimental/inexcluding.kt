package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeNodeTransform
import com.oneeyedmen.minutest.RuntimeTest


object SKIP : TestAnnotation, RuntimeNodeTransform {
    override fun <F> applyTo(node: RuntimeNode<F>): RuntimeNode<F> = node.skipped()
}

object FOCUS : TestAnnotation, TopLevelTransform {
    override fun applyTo(node: RuntimeNode<Unit>): RuntimeNode<Unit> = when (node) {
        is RuntimeContext<Unit, *> ->
            node.withTransformedChildren(Inexcluded(defaultToSkip = node.hasAFocusedChild()))
        is RuntimeTest<Unit> ->
            TODO("skipAndFocus on root as test")
    }
}

private class Inexcluded(val defaultToSkip: Boolean) : RuntimeNodeTransform {
    override fun <F> applyTo(node: RuntimeNode<F>): RuntimeNode<F> =
        when (node) {
            is RuntimeContext<F, *> -> applyToContext(node)
            is RuntimeTest<F> -> applyToTest(node)
        }
    
    private fun <PF, F> applyToContext(node: RuntimeContext<PF, F>): RuntimeNode<PF> =
        when {
            FOCUS.appliesTo(node) ->
                node.withTransformedChildren(Inexcluded(defaultToSkip = false))
            node.hasAFocusedChild() ->
                node.withTransformedChildren(this)
            defaultToSkip -> node.skipped()
            else ->
                node.withTransformedChildren(this)
        }
    
    private fun <F> applyToTest(node: RuntimeTest<F>): RuntimeTest<F> =
        when {
            FOCUS.appliesTo(node) -> node
            defaultToSkip -> node.skipped()
            else -> node
        }
}

private fun RuntimeContext<*, *>.hasAFocusedChild() =
    this.hasA(FOCUS::appliesTo)

private fun <F> RuntimeNode<F>.skipped() =
    RuntimeTest<F>(name, annotations) { _, _ -> throw MinutestSkippedException() }

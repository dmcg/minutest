package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest


object SKIP : TestAnnotation, RuntimeNodeTransform {
    override fun <F> applyTo(node: RuntimeNode<F>): RuntimeNode<F> = node.skipped()
}

object FOCUS : TestAnnotation, TopLevelTransform {
    override fun applyTo(node: RuntimeNode<Unit>): RuntimeNode<Unit> = when (node) {
        is RuntimeContext<Unit, *> -> {
            val defaultToSkip = node.hasAFocusedChild()
            @Suppress("UNCHECKED_CAST") //  we couldn't check in the is, Any? is fine
            (node as RuntimeContext<Unit, Any?>).withTransformedChildren { it.inexcluded(defaultToSkip) }
        }
        is RuntimeTest<Unit> -> TODO("skipAndFocus on root as test")
    }
}

private fun RuntimeContext<*, *>.hasAFocusedChild() = this.hasA(FOCUS::appliesTo)

private fun <F> RuntimeNode<F>.inexcluded(defaultToSkip: Boolean): RuntimeNode<F> = when (this) {
    is RuntimeTest<F> -> this.inexcluded(defaultToSkip)
    is RuntimeContext<F, *> -> this.inexcluded(defaultToSkip)
}

private fun <PF, F> RuntimeContext<PF, F>.inexcluded(defaultToSkip: Boolean): RuntimeNode<PF> =
    when {
        FOCUS.appliesTo(this) ->
            this.withTransformedChildren { it.inexcluded(defaultToSkip = false) }
        this.hasAFocusedChild() ->
            this.withTransformedChildren { it.inexcluded(defaultToSkip) }
        defaultToSkip -> this.skipped()
        else ->
            this.withTransformedChildren { it.inexcluded(defaultToSkip) }
    }

private fun <F> RuntimeTest<F>.inexcluded(defaultToSkip: Boolean): RuntimeTest<F> =
    when {
        FOCUS.appliesTo(this) -> this
        defaultToSkip -> this.skipped()
        else -> this
    }

private fun <F> RuntimeNode<F>.skipped() = RuntimeTest<F>(name, annotations) { _, _ ->
    throw MinutestSkippedException()
}
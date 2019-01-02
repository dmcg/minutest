package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest


object SKIP : TestAnnotation, RuntimeNodeTransform {
    override fun <F> applyTo(node: RuntimeNode<F>): RuntimeNode<F> = when(node) {
        is RuntimeTest -> node.skipped()
        is RuntimeContext<F, *> -> node.skipped()
    }
}

object FOCUS : TestAnnotation, TopLevelTransform {
    override fun applyTo(node: RuntimeNode<Unit>): RuntimeNode<Unit> = skipAndFocus<Unit>(node)
}

private fun <F> skipAndFocus(rootContext: (RuntimeNode<Unit>)): RuntimeNode<Unit> =
    if (SKIP.appliesTo(rootContext)) {
        rootContext.skipped()
    } else {
        when (rootContext) {
            is RuntimeContext<Unit, *> -> {
                val defaultToSkip = rootContext.hasAFocusedChild()
                (rootContext as RuntimeContext<Unit, F>).withTransformedChildren { it.inexcluded(defaultToSkip) }
            }
            is RuntimeTest<Unit> -> TODO()
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

private fun <F> RuntimeNode<F>.skipped() = when (this) {
    is RuntimeTest<F> -> this.skipped()
    is RuntimeContext<F, *> -> this.skipped()
}

private fun <F> RuntimeTest<F>.skipped() = skipper<F>(name, annotations)

private fun <F> skipper(name: String, annotations: List<TestAnnotation>) = RuntimeTest<F>(name, annotations) { _, _ ->
    throw MinutestSkippedException()
}

private fun <PF, F> RuntimeContext<PF, F>.skipped() = skipper<PF>(name, annotations)

package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest


object SKIP : TestAnnotation
object FOCUS : TestAnnotation

fun <F> skipAndFocus(): (RuntimeContext<F>) -> RuntimeContext<F> = { inexclude(it) }

fun <F> inexclude(root: RuntimeContext<F>): RuntimeContext<F> =
    if (SKIP.appliesTo(root)) {
        root.skipped()
    } else {
        val defaultToSkip = root.hasAFocusedChild()
        root.withTransformedChildren { it.inexcluded(defaultToSkip) }
    }


private fun RuntimeNode.hasAFocusedChild(): Boolean = when (this) {
    is RuntimeTest -> FOCUS.appliesTo(this)
    is RuntimeContext<*> -> hasAFocusedChild()
}

private fun RuntimeContext<*>.hasAFocusedChild() = FOCUS.appliesTo(this) || children.hasAFocusedChild()

private fun List<RuntimeNode>.hasAFocusedChild() = find { it.hasAFocusedChild() } != null

private fun RuntimeNode.inexcluded(defaultToSkip: Boolean): RuntimeNode = when (this) {
    is RuntimeTest -> inexcluded(defaultToSkip)
    is RuntimeContext<*> -> inexcluded(defaultToSkip)
}

private fun RuntimeContext<*>.inexcluded(defaultToSkip: Boolean) =
    when {
        FOCUS.appliesTo(this) ->
            this.withTransformedChildren { it.inexcluded(defaultToSkip = false) }
        hasAFocusedChild() ->
            this.withTransformedChildren { it.inexcluded(defaultToSkip) }
        defaultToSkip || SKIP.appliesTo(this) ->
            this.skipped()
        else ->
            this.withTransformedChildren { it.inexcluded(defaultToSkip) }
    }

private fun RuntimeTest.inexcluded(defaultToSkip: Boolean) =
    when {
        FOCUS.appliesTo(this) -> this
        defaultToSkip || SKIP.appliesTo(this) -> SkippingTest(this.name, this.parent, this.properties)
        else -> this
    }

private fun <F> RuntimeContext<F>.skipped() = this.adopting(listOf(SkippingTest("skipping ${this.name}",
    this,
    this.properties)))


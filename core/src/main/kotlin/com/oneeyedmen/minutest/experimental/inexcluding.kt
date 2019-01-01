package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.RuntimeContextWrapper
import org.opentest4j.TestAbortedException


object SKIP : TestAnnotation, RuntimeTestTransform<Any?>, RuntimeContextTransform<Any?, Any?> {
    override fun apply(test: RuntimeTest<Any?>): RuntimeTest<Any?> = test.skipped()
    override fun apply(context: RuntimeContext<Any?, Any?>): RuntimeContext<Any?, Any?> = context.skipped()
}

object FOCUS : TestAnnotation

fun <F> skipAndFocus(): (RuntimeContext<Unit, F>) -> RuntimeContext<Unit, F> = { rootContext ->
    if (SKIP.appliesTo(rootContext)) {
        rootContext.skipped()
    } else {
        val defaultToSkip = rootContext.hasAFocusedChild()
        rootContext.withTransformedChildren { it.inexcluded(defaultToSkip) }
    }
}


private fun RuntimeNode<*>.hasAFocusedChild(): Boolean = when (this) {
    is RuntimeTest -> FOCUS.appliesTo(this)
    is RuntimeContext<*, *> -> hasAFocusedChild()
}

private fun RuntimeContext<*, *>.hasAFocusedChild() = FOCUS.appliesTo(this) || children.hasAFocusedChild()

private fun List<RuntimeNode<*>>.hasAFocusedChild() = find { it.hasAFocusedChild() } != null

private fun <F> RuntimeNode<F>.inexcluded(defaultToSkip: Boolean): RuntimeNode<F> = when (this) {
    is RuntimeTest<F> -> this.inexcluded(defaultToSkip)
    is RuntimeContext<F, *> -> this.inexcluded(defaultToSkip)
}

private fun <PF, F> RuntimeContext<PF, F>.inexcluded(defaultToSkip: Boolean): RuntimeContext<PF, F> =
    when {
        FOCUS.appliesTo(this) ->
            this.withTransformedChildren { it.inexcluded(defaultToSkip = false) }
        hasAFocusedChild() ->
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

private fun <F> RuntimeTest<F>.skipped() = skipper<F>(name, annotations)

private fun <F> skipper(name: String, properties: List<TestAnnotation>) = RuntimeTest<F>(name, properties) { _, _ ->
    throw TestAbortedException("skipped")
}

private fun <PF, F> RuntimeContext<PF, F>.skipped() = RuntimeContextWrapper(this,
    children = listOf(skipper("skipping ${this.name}", emptyList()))
)

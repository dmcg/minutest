package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.Test

internal object RootContext : ParentContext<Unit> {
    override val name = ""
    override val parent = null
    override fun runTest(test: Test<Unit>) = test(Unit)
}

fun <F> topLevelContext(
    name: String,
    isUnit: Boolean,
    builder: Context<Unit, F>.() -> Unit
): Context<Unit, F> =
    MiContext(name, RootContext, fixtureFunFor<F>(isUnit)).apply(builder)


@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFunFor(isUnit: Boolean): (Unit.() -> F)? = if (isUnit) {{ Unit as F }} else null
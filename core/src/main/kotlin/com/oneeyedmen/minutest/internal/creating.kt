package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.Test

internal object RootContext : ParentContext<Unit> {
    override val name = ""
    override val parent: Nothing? = null
    override fun runTest(test: Test<Unit>) = test(Unit)
}

fun <F> topLevelContext(
    name: String,
    isUnit: Boolean,
    builder: Context<Unit, F>.() -> Unit
): Context<Unit, F> = ContextBuilder<Unit, F>(name, fixtureFunFor(isUnit), false).apply(builder)

fun <F> topLevelContext(
    name: String,
    fixture: F,
    builder: Context<Unit, F>.() -> Unit
): Context<Unit, F> =
    topLevelContext<F>(name, { fixture }, builder)

fun <F> topLevelContext(
    name: String,
    fixtureBuilder: (Unit.() -> F)?,
    builder: Context<Unit, F>.() -> Unit
): Context<Unit, F> =
    ContextBuilder(name, fixtureBuilder, true).apply(builder)


@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFunFor(isUnit: Boolean): (Unit.() -> F)? = if (isUnit) {{ Unit as F }} else null
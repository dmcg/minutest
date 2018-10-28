package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.TestContext

object RootContext : ParentContext<Unit> {
    override val name: String = ""
    override fun runTest(test: Unit.() -> Unit) = test(Unit)
}

/**
 * Build a test context out of context.
 */
internal fun <F> topContext(
    name: String,
    fixtureFn: (Unit.() -> F)? = null,
    builder: TestContext<Unit, F>.() -> Unit
) = MiContext(name, RootContext, fixtureFn).apply { builder() }
package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test

internal class MinuTest<F>(
    override val name: String,
    val context: ParentContext<F>,
    val f: F.() -> F
) : Test<F>, Node {

    override fun invoke(fixture: F): F =
        f(fixture)

    override fun toRuntimeNode() =
        RuntimeTest(this.name) { context.runTest(f) }
}
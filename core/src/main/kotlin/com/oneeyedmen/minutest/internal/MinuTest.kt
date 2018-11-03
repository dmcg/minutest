package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test

internal class MinuTest<F>(
    override val name: String,
    private val context: ParentContext<F>,
    private val f: F.() -> F

) : Test<F>, Node, (F)-> F by f {
    override val parent = context
    override fun toRuntimeNode() = RuntimeTest(this.name) { context.runTest(this) }
}

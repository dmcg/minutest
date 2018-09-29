package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Test

internal class MinuTest<F>(
    override val name: String,
    val f: F.() -> F
) : Test<F> {
    override fun invoke(fixture: F): F = f(fixture)
}
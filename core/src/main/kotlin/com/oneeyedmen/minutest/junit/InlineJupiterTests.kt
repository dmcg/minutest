package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext

/**
 * Convenience class to reduce boilerplate
 */
abstract class InlineJupiterTests<F>(
    builder: TestContext<Unit, F>.() -> Unit
) : JupiterTests {

    override val tests = context(builder)
}
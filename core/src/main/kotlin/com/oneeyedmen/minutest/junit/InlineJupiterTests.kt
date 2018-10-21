package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.asKType
import com.oneeyedmen.minutest.testContext

/**
 * Convenience class to reduce boilerplate
 */
abstract class InlineJupiterTests<F>(
    builder: TestContext<F>.() -> Unit,
    fixtureIsNullable: Boolean = false
) : JupiterTests, IKnowMyGenericClass<F> {

    @Suppress("LeakingThis")
    override val tests = testContext(
        rootContextName,
        myGenericClass().asKType(fixtureIsNullable),
        builder)
}
package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext

/**
 * EXPERIMENTAL Base class for tests that you want run with JUnit 5
 */
@Deprecated("Use InlineJupiterTests instead", level = DeprecationLevel.WARNING)
abstract class JUnitTests<F>(
    builder: TestContext<F>.() -> Unit,
    fixtureIsNullable: Boolean = false
) : InlineJupiterTests<F>(builder, fixtureIsNullable)

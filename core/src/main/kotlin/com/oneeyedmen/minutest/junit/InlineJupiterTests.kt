package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.internal.asKType
import com.oneeyedmen.minutest.internal.topLevelContext

/**
 * Convenience class to reduce boilerplate
 */
abstract class InlineJupiterTests<F>(
    builder: Context<Unit, F>.() -> Unit
) : JupiterTests, IKnowMyGenericClass<F> {

    @Suppress("LeakingThis")
    override val tests = topLevelContext(
        javaClass.canonicalName,
        myGenericClass().asKType(false),
        builder)
}

package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.internal.top
import kotlin.reflect.KClass

/**
 * Convenience class to reduce boilerplate
 */
abstract class InlineJupiterTests<F>(
    builder: Context<Unit, F>.() -> Unit
) : JupiterTests, IKnowMyGenericClass<F> {

    @Suppress("LeakingThis")
    override val tests = top(
        javaClass.canonicalName,
        fixtureFnFor(myGenericClass()),
        builder)
}

@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFnFor(kClass: KClass<*>): ((Unit) -> F)? =
    if (kClass.isInstance(Unit)) {
        { Unit as F }
    } else null


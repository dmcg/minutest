package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.TopLevelContextBuilder
import com.oneeyedmen.minutest.internal.askType

/**
 * The entry point to Minutest - defines a context that is not nested within a parent context.
 */
inline fun <reified F> rootContext(
    noinline transform: (RuntimeContext<Unit, F>) -> RuntimeContext<Unit, F> = { it },
    name: String = "root",
    noinline builder: Context<Unit, F>.() -> Unit
) = TopLevelContextBuilder(name, askType<F>(), builder, transform)

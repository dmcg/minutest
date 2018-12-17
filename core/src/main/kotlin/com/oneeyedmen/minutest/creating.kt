package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.TopLevelContextBuilder


inline fun <reified F> rootContext(
    noinline transform: (RuntimeContext<F>) -> RuntimeContext<F> = { it },
    name: String = "root",
    noinline builder: Context<Unit, F>.() -> Unit
): TopLevelContextBuilder<F> = TopLevelContextBuilder(name, transform, builder)

package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.TopLevelContextBuilder
import com.oneeyedmen.minutest.internal.askType


inline fun <reified F> rootContext(
    noinline transform: (RuntimeContext<Unit, F>) -> RuntimeContext<Unit, F> = { it },
    name: String = "root",
    noinline builder: Context<Unit, F>.() -> Unit
) = TopLevelContextBuilder(name, askType<F>(), builder, transform)

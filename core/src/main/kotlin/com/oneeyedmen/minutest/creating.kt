package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.TopLevelContextBuilder
import com.oneeyedmen.minutest.internal.askType


inline fun <reified F> rootContext(
    noinline transform: (RuntimeNode) -> RuntimeNode = { it },
    name: String = "root",
    noinline builder: Context<Unit, F>.() -> Unit
) = TopLevelContextBuilder(name, askType<F>(), builder, transform)

package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.TopLevelContextBuilder


inline fun <reified F> rootContext(
    noinline transform: (RuntimeNode) -> RuntimeNode = { it },
    name: String = "root",
    noinline builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit, F> = TopLevelContextBuilder(name, transform, builder)

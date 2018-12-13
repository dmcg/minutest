package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.transformedTopLevelContext


inline fun <reified F> rootContext(
    noinline transform: (RuntimeNode) -> RuntimeNode = { it },
    name: String = "root",
    noinline builder: Context<Unit, F>.() -> Unit
): RootNodeBuilder<F> = transformedTopLevelContext(name, transform, builder)

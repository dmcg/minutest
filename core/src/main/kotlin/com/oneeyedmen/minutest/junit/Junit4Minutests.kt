package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.internal.transformedTopLevelContext

interface Junit4Minutests {
    val tests: NodeBuilder<Unit>
}

/**
 * Define a group of tests.
 */
inline fun <reified F> Junit4Minutests.context(
    noinline transform: (RuntimeNode) -> RuntimeNode = { it },
    noinline builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit> = transformedTopLevelContext(javaClass.canonicalName,
    transform,
    builder)
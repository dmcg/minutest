package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.internal.transformedTopLevelContext


interface JUnitXMinutests {
    val tests: NodeBuilder<Unit>
}

/**
 * Define a [Context] for [JUnitXMinutests] tests.
 *
 * Designed to be called inside a class and to use the name as the class as the name of the context.
 */
inline fun <reified F> Any.context(
    noinline transform: (RuntimeNode) -> RuntimeNode = { it },
    noinline builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit> = transformedTopLevelContext(javaClass.canonicalName, transform, builder)

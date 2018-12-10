package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.rootContext
import kotlin.reflect.full.memberFunctions


interface JUnitXMinutests {
    val tests: NodeBuilder<Unit>
}

/**
 * Define a [Context] for [JUnitXMinutests] tests.
 *
 * Designed to be called inside a class and to use the name as the class as the name of the context.
 */
@Deprecated("Replace with rootContext", ReplaceWith("rootContext(transform, name, builder)", "com.oneeyedmen.minutest.rootContext"))
inline fun <reified F> Any.context(
    noinline transform: (RuntimeNode) -> RuntimeNode = { it },
    noinline builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit> = rootContext(transform, javaClass.canonicalName, builder)


internal fun testMethods(container: Any): List<NodeBuilder<Unit>> = container::class.memberFunctions
    .filter { it.returnType.classifier == NodeBuilder::class }
    .map { it.call(container) as NodeBuilder<Unit> }

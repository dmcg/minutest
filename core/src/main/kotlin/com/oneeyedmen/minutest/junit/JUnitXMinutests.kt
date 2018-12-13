package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.*
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions


interface JUnitXMinutests {
    val tests: NodeBuilder<Unit, *>
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
): NodeBuilder<Unit, *> = rootContext(transform, javaClass.canonicalName, builder)


@Suppress("UNCHECKED_CAST")
internal fun Any.testMethods(): List<KFunction<RootNodeBuilder<*>>> = this::class.memberFunctions
    .filter { it.returnType.classifier == RootNodeBuilder::class } as List<KFunction<RootNodeBuilder<*>>>


internal fun Any.rootContextFromMethods(): RuntimeContext {
    val testMethods = testMethods()
    return when {
        testMethods.isEmpty() -> error("No test methods found")
        testMethods.size == 1 -> toRootNode(testMethods.first())
        else -> LoadedRuntimeContext("", null, emptyMap(),
            testMethods.map { method -> LoadedRuntimeContext(toRootNode(method), name = method.name) },
            onClose = {})

    }
}

private fun Any.toRootNode(method: KFunction<RootNodeBuilder<*>>) =
    method.call(this).buildRootNode() as RuntimeContext



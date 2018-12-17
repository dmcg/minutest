package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.*
import com.oneeyedmen.minutest.internal.TopLevelContextBuilder
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
internal fun Any.testMethods(): List<NodeBuilder<Unit, *>> = this::class.memberFunctions
    .filter { it.returnType.classifier == TopLevelContextBuilder::class }
    .map { it.call(this) as NodeBuilder<Unit, *> }

internal fun Any.rootContextFromMethods(): RuntimeContext<Unit> {
    val testMethodsAsNodes: List<NodeBuilder<Unit, *>> = testMethods()
    val singleNode = when {
        testMethodsAsNodes.isEmpty() -> error("No test methods found")
        testMethodsAsNodes.size > 1 -> error("More than one test method found")
        else -> testMethodsAsNodes.first()
    }
    return singleNode.buildRootNode() as RuntimeContext<Unit>
}

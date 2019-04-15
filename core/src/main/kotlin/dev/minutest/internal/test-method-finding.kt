package dev.minutest.internal

import dev.minutest.Node
import dev.minutest.RootContextBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions


internal fun Any.rootContextFromMethods(): Node<Unit> {
    val contextBuilderMethods = this::class.testMethods()
    return when {
        contextBuilderMethods.isEmpty() -> error("No test methods found")
        contextBuilderMethods.size == 1 -> contextBuilderMethods.first().invoke(this).buildNode()
        else -> AmalgamatedRootContext(
            "root",
            contextBuilderMethods.map { method ->
                {
                    method(this).withName(method.name)
                }
            })
    }
}

@Suppress("UNCHECKED_CAST") // reflection
private fun <T : Any> KClass<out T>.testMethods(): List<KFunction1<T, RootContextBuilder>> =
    memberFunctions
        .filter { method ->
            method.returnType.classifier == RootContextBuilder::class &&
                method.parameters.size == 1 &&
                method.visibility == KVisibility.PUBLIC
        }
        .map { method -> method as KFunction1<T, RootContextBuilder> }

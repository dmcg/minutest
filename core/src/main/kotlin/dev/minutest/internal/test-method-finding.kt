package dev.minutest.internal

import dev.minutest.Node
import dev.minutest.RootContextBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions


internal fun Any.rootContextFromMethods(): Node<Unit> {
    val contextBuilderMethods = this::class.testMethods()
    return when {
        contextBuilderMethods.isEmpty() -> error("No test methods found")
        contextBuilderMethods.size == 1 ->
            createSingleRoot(contextBuilderMethods.first())
        else -> AmalgamatedRootContext(
            this::class.qualifiedName!!,
            contextBuilderMethods.map { method ->
                    method.invoke(this).withNameUnlessSpecified(method.name).buildNode()
            })
    }
}

private fun Any.createSingleRoot(function: KFunction1<Any, RootContextBuilder>): Node<Unit> =
    function
        .invoke(this)
        .withNameUnlessSpecified(function.name)
        .buildNode()

private fun KClass<*>.testMethods(): List<KFunction1<Any, RootContextBuilder>> =
    memberFunctions
        .asSequence()
        .filter { method ->
            method.returnType.classifier == RootContextBuilder::class &&
                // only `this` receiver as parameters
                method.parameters.size == 1 && method.parameters[0].kind == KParameter.Kind.INSTANCE &&
                method.visibility == KVisibility.PUBLIC
        }
        .map { method ->
            @Suppress("UNCHECKED_CAST" /* safe, checked has only `this` receiver as argument and correct return type */)
            method as KFunction1<Any, RootContextBuilder>
        }
        .toList()

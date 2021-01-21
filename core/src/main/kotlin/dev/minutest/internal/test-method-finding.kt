package dev.minutest.internal

import dev.minutest.Node
import dev.minutest.RootContextBuilder
import kotlin.reflect.*
import kotlin.reflect.full.memberFunctions


/**
 * Find nodes from the methods on this instance.
 *
 * Flattens the contexts depending on whether one or more methods found
 */
internal fun Any.rootContextFromMethods(): Node<Unit> {
    val contextBuilderMethods = this::class.testMethods()
    return when (contextBuilderMethods.size) {
        0 -> error("No test methods found")
        1 -> createSingleRoot(contextBuilderMethods.first())
        else -> AmalgamatedRootContext(
            this::class.qualifiedName ?: error("Trying find tests in class with no name"),
            contextBuilderMethods.map { method ->
                method.invoke(this).withNameUnlessSpecified(method.name).buildNode()
            }
        )
    }
}

private fun Any.createSingleRoot(function: KFunction1<Any, RootContextBuilder>): Node<Unit> =
    function
        .invoke(this)
        .withNameUnlessSpecified(function.name)
        .buildNode()

private fun KClass<*>.testMethods(): List<KFunction1<Any, RootContextBuilder>> =
    memberFunctions
        .filter(::isTestMethod)
        .map { method ->
            @Suppress(
                "UNCHECKED_CAST"
                /* safe, checked has only `this` receiver as argument and correct return type */
            )
            method as KFunction1<Any, RootContextBuilder>
        }

private fun isTestMethod(method: KFunction<*>) =
    method.returnType.classifier == RootContextBuilder::class &&
        // only `this` receiver as parameters
        method.parameters.size == 1 && method.parameters[0].kind == KParameter.Kind.INSTANCE &&
        method.visibility == KVisibility.PUBLIC

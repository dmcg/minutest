package dev.minutest.internal

import dev.minutest.RootContextBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.kotlinFunction


/**
 * Find nodes from the methods on this instance.
 *
 * Assumes we know receiver
 *
 * Flattens the contexts depending on whether one or more methods found
 */
internal fun Any.rootContextFromMethods(
    filter: (KFunction<RootContextBuilder>) -> Boolean = { true }
): AmalgamatedRootContext? {
    val contextBuilderMethods = this.methodsAsContextBuilderBuilders(filter)
    return when {
        contextBuilderMethods.isEmpty() ->
            null
        else ->
            AmalgamatedRootContext(
                this::class.qualifiedName ?: error("Trying find tests in class with no name"),
                contextBuilderMethods.asSequence().map { method ->
                    method.invoke().buildNode()
                }
            )
    }
}

internal fun rootContextForClass(
    klass: KClass<*>
): AmalgamatedRootContext? =
    klass.constructors.singleOrNull()
        ?.call()
        ?.rootContextFromMethods()


internal fun Any.methodsAsContextBuilderBuilders(
    filter: (KFunction<RootContextBuilder>) -> Boolean
): List<() -> RootContextBuilder> =
    contextBuilderMethods(filter).invokeOn(this)

internal fun List<KFunction<RootContextBuilder>>.invokeOn(o: Any): List<() -> RootContextBuilder> =
    map { method ->
        {
            method.call(o).withNameUnlessSpecified(method.name)
        }
    }

@Suppress("UNCHECKED_CAST")
internal fun Any.contextBuilderMethods(
    filter: (KFunction<RootContextBuilder>) -> Boolean
): List<KFunction<RootContextBuilder>> {
    return this::class.memberFunctions
        .filter {
            it.returnType.classifier == RootContextBuilder::class
                // only `this` receiver as parameters
                && it.parameters.size == 1
                && it.parameters[0].kind == KParameter.Kind.INSTANCE
                && it.visibility == KVisibility.PUBLIC
                && filter(it as KFunction<RootContextBuilder>)
        } as List<KFunction<RootContextBuilder>>
}

@Suppress("UNCHECKED_CAST")
internal fun Class<*>.staticMethodsAsContextBuilderBuilders(
    filter: (KFunction<RootContextBuilder>) -> Boolean
)
    : List<() -> RootContextBuilder> =
    methods
        .mapNotNull { it.kotlinFunction } // horrendously slow for first call
        .filter {
            it.returnType.classifier == RootContextBuilder::class
                // only `this` receiver as parameters
                && it.parameters.isEmpty()
                && it.visibility == KVisibility.PUBLIC
                && filter(it as KFunction<RootContextBuilder>)
        }
        .map { function ->
            {
                (function.call() as RootContextBuilder).withNameUnlessSpecified(function.name)
            }
        }


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
 */
internal fun Any.rootContextFromMethods(
    filter: (KFunction<RootContextBuilder>) -> Boolean = { true }
): AmalgamatedRootContext? {
    val contextBuilderMethods = this::class.contextBuilderMethods(filter)
    return when {
        contextBuilderMethods.isEmpty() ->
            null
        else ->
            lazyRootRootContext(
                this::class.qualifiedName ?: "A class with no name",
                contextBuilderMethods
            ) { arrayOf(this) }
    }
}

internal fun rootContextForClass(
    klass: KClass<*>,
    filter: (KFunction<RootContextBuilder>) -> Boolean = { true }
): AmalgamatedRootContext? {
    val contextBuilderMethods = klass.contextBuilderMethods(filter)
    val constructor = klass.constructors.singleOrNull()
    return when {
        contextBuilderMethods.isEmpty() || constructor == null ->
            null
        else ->
            lazyRootRootContext(
                klass.qualifiedName ?: "A class with no name",
                contextBuilderMethods
            ) { arrayOf(constructor.call()) }
    }
}


internal fun rootContextFromTopLevelFunctions(
    javaClass: Class<*>,
    filter: (KFunction<RootContextBuilder>) -> Boolean = { true }
): AmalgamatedRootContext? {
    val staticBuilders = javaClass.staticMethodsAsContextBuilderBuilders(filter)
    return when {
        staticBuilders.isEmpty() -> null
        else ->
            lazyRootRootContext(
                javaClass.name,
                staticBuilders
            ) { emptyArray() }
    }
}

private fun lazyRootRootContext(
    name: String,
    contextBuilderMethods: List<KFunction<RootContextBuilder>>,
    instanceProvider: () -> Array<Any>
) = AmalgamatedRootContext(
    name
) {
    val instance = instanceProvider.invoke()
    contextBuilderMethods.map { method ->
        method
            .call(*instance)
            .withNameUnlessSpecified(method.name)
            .buildNode()
    }
}


@Suppress("UNCHECKED_CAST")
internal fun <T : Any> KClass<T>.contextBuilderMethods(
    filter: (KFunction<RootContextBuilder>) -> Boolean
): List<KFunction<RootContextBuilder>> {
    return memberFunctions
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
): List<KFunction<RootContextBuilder>> =
    methods
        .mapNotNull { it.kotlinFunction } // horrendously slow for first call
        .filter {
            it.returnType.classifier == RootContextBuilder::class
                && it.parameters.isEmpty()
                && it.visibility == KVisibility.PUBLIC
                && filter(it as KFunction<RootContextBuilder>)
        } as List<KFunction<RootContextBuilder>>


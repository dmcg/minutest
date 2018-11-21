package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.TestDescriptor
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVisibility
import kotlin.reflect.full.createType

inline fun <reified G> asKType() = G::class.asKType(null is G)

fun KClass<*>.asKType(isNullable: Boolean) =
    createType(typeParameters.map { KTypeProjection.STAR }, nullable = isNullable)

fun KType.creator(): (() -> Any)? {
    val classifier = this.classifier as? KClass<*> ?: return null
    if (classifier == Unit::class) return { Unit } // shortcut as we do this a lot
    val objectInstance = try {
        classifier.objectInstance
    }
    catch (x: Exception) {
        null
    }
    return when {
        objectInstance != null -> {
            { objectInstance }
        }
        classifier.visibility != KVisibility.PUBLIC -> null
        else -> classifier.constructors.noArgCtor()?.let { ctor -> { ctor.call() } }
    }
}

private fun Collection<KFunction<Any>>.noArgCtor() =
    find {
        it.visibility == KVisibility.PUBLIC && it.parameters.isEmpty()
    }

@Suppress("UNCHECKED_CAST")
internal fun <F> experimentalFixtureFactoryFor(type: KType): ((Unit, TestDescriptor) -> F)? =
    type.creator()?.let { creator ->
        { _: Unit, _: TestDescriptor -> creator() as F }
    }

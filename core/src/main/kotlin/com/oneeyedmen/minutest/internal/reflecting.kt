package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.TestDescriptor
import kotlin.reflect.*

inline fun <reified G> asKType() = G::class.asKType(null is G)

fun KClass<*>.asKType(isNullable: Boolean): KType = MyKType(this, isNullable)

fun KType.creator(): (() -> Any)? {
    val classifier = this.classifier as? KClass<*> ?: return null
    if (classifier == Unit::class) return { Unit } // shortcut as we do this a lot
    val objectInstance = try {
        classifier.objectInstance
    } catch (x: Exception) {
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

internal data class MyKType(override val classifier: KClassifier, override val isMarkedNullable: Boolean) : KType {
    override val arguments: List<KTypeProjection> = emptyList()
}

@Suppress("UNCHECKED_CAST")
internal fun <F> experimentalFixtureFactoryFor(type: KType): ((Unit, TestDescriptor) -> F)? =
    type.creator()?.let { creator ->
        { _: Unit, _: TestDescriptor -> creator() as F }
    }
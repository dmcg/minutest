package dev.minutest.internal

import dev.minutest.TestDescriptor
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility

@PublishedApi internal inline fun <reified G> askType() = FixtureType(G::class, null is G)

@PublishedApi internal data class FixtureType(internal val classifier: KClass<*>, internal val isMarkedNullable: Boolean)

internal fun FixtureType.creator(): (() -> Any)? {
    val classifier = this.classifier
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

@Suppress("UNCHECKED_CAST")
internal fun <F> experimentalFixtureFactoryFor(type: FixtureType): ((Unit, TestDescriptor) -> F)? =
    type.creator()?.let { creator ->
        { _: Unit, _: TestDescriptor -> creator() as F }
    }
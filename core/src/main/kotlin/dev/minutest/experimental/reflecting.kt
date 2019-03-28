package dev.minutest.experimental

import dev.minutest.TestDescriptor
import dev.minutest.internal.FixtureType
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility

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

@Suppress("UNCHECKED_CAST", "UNUSED")
internal fun <F> experimentalFixtureFactoryFor(type: FixtureType): ((Unit, TestDescriptor) -> F)? =
    type.creator()?.let { creator ->
        { _: Unit, _: TestDescriptor -> creator() as F }
    }
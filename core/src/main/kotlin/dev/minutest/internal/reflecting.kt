package dev.minutest.internal

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubclassOf

@PublishedApi internal inline fun <reified F> askType(): FixtureType<F> = FixtureType(F::class, null is F)

@PublishedApi internal data class FixtureType<F>(
    internal val classifier: KClass<*>,
    internal val isNullable: Boolean
) {

    fun isSubtypeOf(other: FixtureType<*>) =
        (if (this.isNullable) other.isNullable else true) &&
            this.classifier.isSubclassOf(other.classifier)

    override fun toString() = "FixtureType($qualifiedName)"

    val qualifiedName get() = "${classifier.qualifiedName}${if (isNullable) "?" else ""}"

    @Suppress("UNCHECKED_CAST")
    internal fun creator(): (() -> F)? {
        val classifier = this.classifier
        if (classifier == Unit::class) return { Unit as F } // shortcut as we do this a lot
        val objectInstance = try {
            classifier.objectInstance
        } catch (x: Exception) {
            null
        }
        return when {
            objectInstance != null -> {
                { objectInstance as F }
            }
            classifier.visibility != KVisibility.PUBLIC -> null
            else -> classifier.constructors.noArgCtor()?.let { ctor -> { ctor.call() as F } }
        }
    }
}

private fun <F> Collection<KFunction<F>>.noArgCtor(): KFunction<F>? =
    find {
        it.visibility == KVisibility.PUBLIC && it.parameters.isEmpty()
    }

internal val unitFixtureType = askType<Unit>()

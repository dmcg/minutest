package dev.minutest.internal

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@PublishedApi internal inline fun <reified G> askType() = FixtureType(G::class, null is G)

@PublishedApi internal data class FixtureType(internal val classifier: KClass<*>, internal val isNullable: Boolean) {
    fun isSubtypeOf(other: FixtureType) =
        (if (this.isNullable) other.isNullable else true) &&
            this.classifier.isSubclassOf(other.classifier)
}

internal val unitFixtureType = askType<Unit>()

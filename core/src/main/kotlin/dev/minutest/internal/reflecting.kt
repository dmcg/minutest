package dev.minutest.internal

import kotlin.reflect.KClass

@PublishedApi internal inline fun <reified G> askType() = FixtureType(G::class, null is G)

@PublishedApi internal data class FixtureType(internal val classifier: KClass<*>, internal val isMarkedNullable: Boolean)
